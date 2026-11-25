package com.kalasetu.concurrency;

import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.model.CraftProduct;
import com.kalasetu.domain.model.Order;
import com.kalasetu.payment.TestPaymentGatewayService;
import com.kalasetu.persistence.InMemoryMarketplaceRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyModuleTest {

    @Test
    void testConcurrentOrdersPreventOverselling() throws Exception {
        InMemoryMarketplaceRepository repo = new InMemoryMarketplaceRepository();
        TestPaymentGatewayService gw = new TestPaymentGatewayService();
        try (BackgroundNotificationWorker notif = new BackgroundNotificationWorker(repo);
             ConcurrentOrderProcessor processor = new ConcurrentOrderProcessor(repo, gw, notif)) {

            // Patola dupatta starts with stock = 6
            UUID patolaId = UUID.fromString("44444444-4444-4444-4444-444444444402");
            UUID buyerId = UUID.fromString("22222222-2222-2222-2222-222222222201");

            // Fire 8 concurrent threads each trying to buy quantity 1 (stock is 6 => 6 succeed, 2 get OutOfStockException)
            List<CompletableFuture<Order>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                futures.add(processor.submitOrderAsync(new ConcurrentOrderProcessor.CheckoutCommand(
                        buyerId,
                        List.of(new ConcurrentOrderProcessor.LineRequest(patolaId, 1)),
                        "12 Ashram Road, Ahmedabad",
                        "UPI_TEST",
                        "aarav@okicici",
                        false,
                        true
                )));
            }

            int successes = 0;
            int outOfStockFailures = 0;
            for (CompletableFuture<Order> f : futures) {
                try {
                    Order o = f.join();
                    assertEquals("CONFIRMED", o.getOrderStatus());
                    successes++;
                } catch (CompletionException ce) {
                    if (ce.getCause() instanceof OutOfStockException) {
                        outOfStockFailures++;
                    } else {
                        fail("Unexpected exception: " + ce.getCause());
                    }
                }
            }

            assertEquals(6, successes);
            assertEquals(2, outOfStockFailures);

            CraftProduct updated = repo.findProductById(patolaId).orElseThrow();
            assertEquals(0, updated.getStockQty());

            // Give background notification daemon a short moment to flush queue
            Thread.sleep(350);
            assertTrue(notif.getProcessedCount() >= 6);
            assertFalse(repo.findRecentNotifications(20).isEmpty());
        }
    }
}
