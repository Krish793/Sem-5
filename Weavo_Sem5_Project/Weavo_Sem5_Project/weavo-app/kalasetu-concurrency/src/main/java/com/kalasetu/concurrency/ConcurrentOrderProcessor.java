package com.kalasetu.concurrency;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.exception.PaymentFailedException;
import com.kalasetu.domain.model.*;
import com.kalasetu.domain.security.InputSanitizer;
import com.kalasetu.payment.PaymentReceipt;
import com.kalasetu.payment.PaymentRequest;
import com.kalasetu.payment.TestPaymentGatewayService;
import com.kalasetu.persistence.MarketplaceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Multithreaded Order Processing Engine using ExecutorService + per-product ReentrantLocks.
 */
public class ConcurrentOrderProcessor implements AutoCloseable {
    public record LineRequest(UUID productId, int quantity) {}

    public record CheckoutCommand(
            UUID customerId,
            List<LineRequest> lines,
            String shippingAddress,
            String paymentMethodCode,
            String paymentToken,
            boolean simulatePaymentFailure,
            boolean dpdpConsentAccepted
    ) {}

    private final MarketplaceRepository repository;
    private final TestPaymentGatewayService paymentGateway;
    private final BackgroundNotificationWorker notificationWorker;
    private final ExecutorService executor;
    private final ConcurrentHashMap<UUID, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    public ConcurrentOrderProcessor(MarketplaceRepository repository,
                                    TestPaymentGatewayService paymentGateway,
                                    BackgroundNotificationWorker notificationWorker) {
        this.repository = repository;
        this.paymentGateway = paymentGateway;
        this.notificationWorker = notificationWorker;
        AtomicInteger threadIdx = new AtomicInteger(1);
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "weavo-order-worker-" + threadIdx.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
    }

    public CompletableFuture<Order> submitOrderAsync(CheckoutCommand cmd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return processCheckoutSync(cmd);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public Order processCheckoutSync(CheckoutCommand cmd)
            throws InvalidInputException, OutOfStockException, PaymentFailedException {
        if (!cmd.dpdpConsentAccepted()) {
            throw new InvalidInputException("dpdpConsent", "Explicit DPDP Act 2023 consent is required before placing an order");
        }
        if (cmd.lines() == null || cmd.lines().isEmpty()) {
            throw new InvalidInputException("items", "Order must contain at least one craft item");
        }
        String cleanAddress = InputSanitizer.sanitizeText("shippingAddress", cmd.shippingAddress(), 300);

        // Lock products in deterministic UUID order to prevent deadlocks across threads
        List<LineRequest> sortedLines = cmd.lines().stream()
                .sorted(Comparator.comparing(LineRequest::productId))
                .toList();

        List<ReentrantLock> acquired = new ArrayList<>();
        for (LineRequest lr : sortedLines) {
            InputSanitizer.requirePositiveInt("quantity", lr.quantity(), 50);
            ReentrantLock lock = productLocks.computeIfAbsent(lr.productId(), k -> new ReentrantLock());
            lock.lock();
            acquired.add(lock);
        }

        try {
            BigDecimal itemsSubtotal = BigDecimal.ZERO;
            BigDecimal totalCommission = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (LineRequest lr : sortedLines) {
                CraftProduct product = repository.findProductById(lr.productId())
                        .orElseThrow(() -> new InvalidInputException("productId", "Product not found: " + lr.productId()));

                if (product.getStockQty() < lr.quantity()) {
                    throw new OutOfStockException(product.getTitle(), product.getStockQty(), lr.quantity());
                }

                BigDecimal lineTotal = product.getPriceInr().multiply(BigDecimal.valueOf(lr.quantity()));
                BigDecimal commRate = repository.findArtisanById(product.getArtisanId())
                        .map(ap -> ap.getCommissionRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .orElse(new BigDecimal("0.10"));
                BigDecimal lineCommission = lineTotal.multiply(commRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal payout = lineTotal.subtract(lineCommission);

                itemsSubtotal = itemsSubtotal.add(lineTotal);
                totalCommission = totalCommission.add(lineCommission);

                OrderItem oi = new OrderItem();
                oi.setProductId(product.getId());
                oi.setArtisanId(product.getArtisanId());
                oi.setProductTitle(product.getTitle());
                oi.setQuantity(lr.quantity());
                oi.setUnitPriceInr(product.getPriceInr());
                oi.setLineTotalInr(lineTotal);
                oi.setArtisanPayoutInr(payout);
                orderItems.add(oi);
            }

            BigDecimal shippingFee = itemsSubtotal.compareTo(new BigDecimal("2500")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("99.00");
            BigDecimal grandTotal = itemsSubtotal.add(shippingFee);
            String orderNumber = "WV-" + (2026) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            // Charge strictly in TEST MODE
            PaymentReceipt receipt = paymentGateway.chargeTestMode(new PaymentRequest(
                    orderNumber, grandTotal, cmd.paymentMethodCode(), cmd.paymentToken(), cmd.simulatePaymentFailure()
            ));

            // Atomically decrement stock (with Supabase RPC or synchronized in-memory check)
            try {
                for (LineRequest lr : sortedLines) {
                    repository.decrementStockAtomic(lr.productId(), lr.quantity());
                }
            } catch (OutOfStockException oos) {
                paymentGateway.refundTestMode(receipt.getMethodCode(), receipt.getTransactionRef(), grandTotal);
                throw oos;
            }

            Order order = new Order();
            order.setOrderNumber(orderNumber);
            order.setCustomerId(cmd.customerId());
            order.setTotalAmountInr(grandTotal);
            order.setPlatformCommissionInr(totalCommission);
            order.setShippingFeeInr(shippingFee);
            order.setPaymentMethod(receipt.getMethodCode());
            order.setPaymentStatus(receipt.getStatus());
            order.setTransactionRef(receipt.getTransactionRef());
            order.setOrderStatus("CONFIRMED");
            order.setShippingAddress(cleanAddress);
            order.setItems(orderItems);

            Order saved = repository.saveOrder(order);
            repository.clearCart(cmd.customerId());

            if (notificationWorker != null) {
                notificationWorker.enqueue(new BackgroundNotificationWorker.NotificationTask(
                        saved.getId(),
                        cmd.customerId(),
                        "EMAIL_SIM",
                        "Order " + saved.getOrderNumber() + " confirmed (₹" + grandTotal + ") via "
                                + Thread.currentThread().getName()
                ));
                notificationWorker.enqueue(new BackgroundNotificationWorker.NotificationTask(
                        saved.getId(),
                        cmd.customerId(),
                        "WHATSAPP_SIM",
                        "Namaste! Your village craft order " + saved.getOrderNumber()
                                + " is being packed by the artisan workshop."
                ));
            }

            return saved;
        } finally {
            for (int i = acquired.size() - 1; i >= 0; i--) {
                acquired.get(i).unlock();
            }
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
