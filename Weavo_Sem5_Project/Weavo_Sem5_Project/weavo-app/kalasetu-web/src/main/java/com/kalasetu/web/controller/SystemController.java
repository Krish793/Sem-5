package com.kalasetu.web.controller;

import com.kalasetu.concurrency.BackgroundNotificationWorker;
import com.kalasetu.persistence.MarketplaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    private final MarketplaceRepository repository;
    private final BackgroundNotificationWorker worker;
    private final boolean paymentTestMode;

    public SystemController(MarketplaceRepository repository,
                            BackgroundNotificationWorker worker,
                            @Value("${kalasetu.payment-test-mode:true}") boolean paymentTestMode) {
        this.repository = repository;
        this.worker = worker;
        this.paymentTestMode = paymentTestMode;
    }

    @GetMapping("/status")
    public Map<String, Object> systemStatus() {
        return Map.of(
                "application", "Weavo — From the loom to your living room.",
                "tagline", "From the loom to your living room.",
                "databaseMode", repository.getBackendMode(),
                "paymentMode", paymentTestMode ? "TEST_SANDBOX_ONLY" : "RESTRICTED",
                "notificationWorker", worker.getWorkerThreadName(),
                "modules", List.of(
                        Map.of("module", "kalasetu-domain", "status", "UP", "purpose", "OOP User Hierarchy, Custom Exceptions, DPDP/Security Sanitizer"),
                        Map.of("module", "kalasetu-payment", "status", "UP", "purpose", "PaymentMethod Interface (UPI/Card/Wallet Test Mode)"),
                        Map.of("module", "kalasetu-persistence", "status", "UP", "purpose", repository.getBackendMode() + " + Storage"),
                        Map.of("module", "kalasetu-concurrency", "status", "UP", "purpose", "ExecutorService Pool + ReentrantLocks + Daemon Notification Queue"),
                        Map.of("module", "kalasetu-fileio", "status", "UP", "purpose", "CSV Catalog Import/Export + Order Receipts + Audit Logs"),
                        Map.of("module", "kalasetu-web", "status", "UP", "purpose", "Spring Boot 3 REST Gateway + Module-wise UI")
                )
        );
    }
}
