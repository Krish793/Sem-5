package com.kalasetu.web.controller;

import com.kalasetu.concurrency.ConcurrentOrderProcessor;
import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.UnauthorizedActionException;
import com.kalasetu.domain.model.AuditEvent;
import com.kalasetu.domain.model.CartItem;
import com.kalasetu.domain.model.Order;
import com.kalasetu.domain.model.VerifiedReview;
import com.kalasetu.domain.security.InputSanitizer;
import com.kalasetu.fileio.CatalogFileService;
import com.kalasetu.persistence.MarketplaceRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CartOrderController {
    private final MarketplaceRepository repository;
    private final ConcurrentOrderProcessor orderProcessor;
    private final CatalogFileService fileService;

    public CartOrderController(MarketplaceRepository repository,
                               ConcurrentOrderProcessor orderProcessor,
                               CatalogFileService fileService) {
        this.repository = repository;
        this.orderProcessor = orderProcessor;
        this.fileService = fileService;
    }

    public record CartUpsertRequest(UUID productId, int quantity) {}
    public record CheckoutRequest(
            UUID customerId,
            List<ConcurrentOrderProcessor.LineRequest> lines,
            String shippingAddress,
            String paymentMethodCode,
            String paymentToken,
            boolean simulatePaymentFailure,
            boolean dpdpConsentAccepted
    ) {}
    public record ReviewRequest(UUID productId, UUID customerId, UUID orderId, int rating, String comment) {}

    @GetMapping("/cart/{customerId}")
    public List<CartItem> getCart(@PathVariable UUID customerId) {
        return repository.findCartByCustomer(customerId);
    }

    @PostMapping("/cart/{customerId}")
    public CartItem upsertCart(@PathVariable UUID customerId, @RequestBody CartUpsertRequest req)
            throws InvalidInputException {
        InputSanitizer.requirePositiveInt("quantity", req.quantity(), 50);
        return repository.upsertCartItem(customerId, req.productId(), req.quantity());
    }

    @DeleteMapping("/cart/{customerId}/{productId}")
    public Map<String, String> removeCartItem(@PathVariable UUID customerId, @PathVariable UUID productId) {
        repository.removeCartItem(customerId, productId);
        return Map.of("status", "removed");
    }

    @PostMapping("/orders/checkout")
    public Order checkout(@RequestBody CheckoutRequest req) throws Exception {
        if (req.customerId() == null) {
            throw new InvalidInputException("customerId", "Customer login is required");
        }
        List<ConcurrentOrderProcessor.LineRequest> lines = req.lines();
        if (lines == null || lines.isEmpty()) {
            List<CartItem> cart = repository.findCartByCustomer(req.customerId());
            lines = cart.stream()
                    .map(ci -> new ConcurrentOrderProcessor.LineRequest(ci.getProductId(), ci.getQuantity()))
                    .toList();
        }
        ConcurrentOrderProcessor.CheckoutCommand cmd = new ConcurrentOrderProcessor.CheckoutCommand(
                req.customerId(),
                lines,
                req.shippingAddress(),
                req.paymentMethodCode(),
                req.paymentToken(),
                req.simulatePaymentFailure(),
                req.dpdpConsentAccepted()
        );
        Order saved = orderProcessor.processCheckoutSync(cmd);
        fileService.writeOrderReceipt(saved);

        AuditEvent ev = new AuditEvent();
        ev.setActorUserId(req.customerId());
        ev.setEventType("ORDER_CHECKOUT_CONFIRMED");
        ev.setEntityType("ORDER");
        ev.setEntityId(saved.getOrderNumber());
        ev.setDetails(Map.of("amountInr", saved.getTotalAmountInr(), "paymentMethod", saved.getPaymentMethod()));
        repository.saveAuditEvent(ev);
        fileService.appendAuditLog(ev);

        return saved;
    }

    @GetMapping("/orders/customer/{customerId}")
    public List<Order> customerOrders(@PathVariable UUID customerId) {
        return repository.findOrdersByCustomer(customerId);
    }

    @GetMapping("/orders")
    public List<Order> allOrders() {
        return repository.findAllOrders();
    }

    @GetMapping(value = "/orders/{orderNumber}/receipt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String orderReceipt(@PathVariable String orderNumber) throws Exception {
        return fileService.readOrderReceipt(orderNumber);
    }

    @PostMapping("/reviews")
    public VerifiedReview submitVerifiedReview(@RequestBody ReviewRequest req)
            throws InvalidInputException, UnauthorizedActionException {
        InputSanitizer.requirePositiveInt("rating", req.rating(), 5);
        String cleanComment = InputSanitizer.sanitizeText("comment", req.comment(), 500);

        Order order = repository.findOrderById(req.orderId())
                .orElseThrow(() -> new UnauthorizedActionException("Only buyers with a valid order can post verified reviews"));
        if (!order.getCustomerId().equals(req.customerId())) {
            throw new UnauthorizedActionException("Cannot review using another customer's order");
        }
        boolean orderedProduct = order.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(req.productId()));
        if (!orderedProduct) {
            throw new UnauthorizedActionException("Anti-fake-review policy: you must purchase this craft before reviewing");
        }

        VerifiedReview review = new VerifiedReview();
        review.setProductId(req.productId());
        review.setCustomerId(req.customerId());
        review.setOrderId(req.orderId());
        review.setRating(req.rating());
        review.setComment(cleanComment);
        review.setVerifiedPurchase(true);
        review.setModerationStatus("APPROVED");
        return repository.saveReview(review);
    }
}
