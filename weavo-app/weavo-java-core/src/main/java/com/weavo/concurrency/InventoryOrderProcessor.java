package com.weavo.concurrency;

import com.weavo.exception.OutOfStockException;
import com.weavo.exception.PaymentFailedException;
import com.weavo.model.CraftProduct;
import com.weavo.model.Order;
import com.weavo.payment.PaymentGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Multithreaded Inventory and Order Processor demonstrating:
 * 1. ThreadPoolExecutor / ExecutorService
 * 2. Callable & Future concurrency patterns
 * 3. ReentrantLock & Atomic variables to prevent race conditions during festival flash sales.
 */
public class InventoryOrderProcessor {
    private final ExecutorService threadPool;
    private final ReentrantLock inventoryLock = new ReentrantLock();
    private final AtomicInteger successfulOrdersCount = new AtomicInteger(0);
    private final AtomicInteger rejectedOrdersCount = new AtomicInteger(0);

    public InventoryOrderProcessor(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Simulates concurrent purchase of a limited artisan craft.
     * Multiple customer threads attempt to order concurrently.
     */
    public boolean processSingleOrder(Order order, CraftProduct product, int quantity, 
                                      PaymentGateway gateway, boolean simulatePaymentFail) 
                                      throws OutOfStockException, PaymentFailedException {
        // Critical section protected by ReentrantLock
        inventoryLock.lock();
        try {
            if (product.getStockQty() < quantity) {
                rejectedOrdersCount.incrementAndGet();
                throw new OutOfStockException(product.getId(), quantity, product.getStockQty());
            }

            // Deduct stock safely
            product.decrementStock(quantity);
            order.addItem(product, quantity);

            // Execute payment
            gateway.processPayment(order, order.getTotalAmountInr(), simulatePaymentFail);
            successfulOrdersCount.incrementAndGet();
            return true;
        } finally {
            inventoryLock.unlock();
        }
    }

    /**
     * Runs a multi-threaded stress test simulating N concurrent buyer threads.
     */
    public void runConcurrentStressTest(CraftProduct product, PaymentGateway gateway, 
                                        int totalThreads, int orderQuantityPerThread) {
        System.out.printf("[Multithreading] Launching %d concurrent buyer threads competing for %s (Initial Stock: %d)...\n",
                totalThreads, product.getTitle(), product.getStockQty());

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 1; i <= totalThreads; i++) {
            final int buyerId = i;
            tasks.add(() -> {
                String threadName = Thread.currentThread().getName();
                Order order = new Order("WV-ORD-" + buyerId, "BUYER-" + buyerId);
                try {
                    boolean success = processSingleOrder(order, product, orderQuantityPerThread, gateway, false);
                    System.out.printf("  ✓ [%s] Buyer-%02d successfully purchased 1 unit! Txn: %s | Remaining Stock: %d\n",
                            threadName, buyerId, order.getTransactionRef(), product.getStockQty());
                    return success;
                } catch (OutOfStockException e) {
                    System.out.printf("  ✗ [%s] Buyer-%02d blocked by OutOfStockException: %s\n",
                            threadName, buyerId, e.getMessage());
                    return false;
                } catch (PaymentFailedException e) {
                    System.out.printf("  ✗ [%s] Buyer-%02d payment failed: %s\n", threadName, buyerId, e.getMessage());
                    return false;
                }
            });
        }

        try {
            List<Future<Boolean>> futures = threadPool.invokeAll(tasks);
            for (Future<Boolean> f : futures) {
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int getSuccessfulOrdersCount() { return successfulOrdersCount.get(); }
    public int getRejectedOrdersCount() { return rejectedOrdersCount.get(); }

    public void shutdown() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
