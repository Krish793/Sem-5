package com.kalasetu.web.controller;

import com.kalasetu.concurrency.BackgroundNotificationWorker;
import com.kalasetu.concurrency.ConcurrentOrderProcessor;
import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.UnauthorizedActionException;
import com.kalasetu.domain.model.*;
import com.kalasetu.domain.security.InputSanitizer;
import com.kalasetu.fileio.CatalogFileService;
import com.kalasetu.persistence.MarketplaceRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RestController
@RequestMapping("/api")
public class ArtisanAdminController {
    private final MarketplaceRepository repository;
    private final ConcurrentOrderProcessor orderProcessor;
    private final BackgroundNotificationWorker notificationWorker;
    private final CatalogFileService fileService;

    public ArtisanAdminController(MarketplaceRepository repository,
                                  ConcurrentOrderProcessor orderProcessor,
                                  BackgroundNotificationWorker notificationWorker,
                                  CatalogFileService fileService) {
        this.repository = repository;
        this.orderProcessor = orderProcessor;
        this.notificationWorker = notificationWorker;
        this.fileService = fileService;
    }

    public record CreateProductRequest(
            UUID artisanUserId, UUID categoryId, String title, String craftType,
            String originVillage, boolean giCertified, String description,
            BigDecimal priceInr, int stockQty, String imageUrl, boolean featuredFestival
    ) {}

    public record KycDecisionRequest(String kycStatus) {}
    public record ConcurrencyDemoRequest(UUID productId, UUID customerId, int threads) {}

    @PostMapping("/artisan/products")
    public CraftProduct addProduct(@RequestBody CreateProductRequest req) throws Exception {
        ArtisanProfile ap = repository.findArtisanByUserId(req.artisanUserId())
                .orElseThrow(() -> new UnauthorizedActionException("Artisan profile not found for user"));
        if (!"VERIFIED".equalsIgnoreCase(ap.getKycStatus())) {
            throw new UnauthorizedActionException("Artisan KYC must be VERIFIED before publishing products");
        }
        String title = InputSanitizer.sanitizeText("title", req.title(), 140);
        String craftType = InputSanitizer.sanitizeText("craftType", req.craftType(), 80);
        String village = InputSanitizer.sanitizeText("originVillage", req.originVillage(), 100);
        String desc = InputSanitizer.sanitizeText("description", req.description(), 1000);
        InputSanitizer.requirePositiveAmount("priceInr", req.priceInr());
        InputSanitizer.requirePositiveInt("stockQty", req.stockQty(), 10000);

        CraftProduct p = new CraftProduct();
        p.setArtisanId(ap.getId());
        p.setCategoryId(req.categoryId());
        p.setSku("WV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        p.setTitle(title);
        p.setCraftType(craftType);
        p.setOriginVillage(village);
        p.setGiCertified(req.giCertified());
        p.setDescription(desc);
        p.setPriceInr(req.priceInr());
        p.setStockQty(req.stockQty());
        p.setImageUrl((req.imageUrl() != null && !req.imageUrl().isBlank()) ? req.imageUrl() : "/images/bandhani-saree.png");
        p.setStoragePath("artisan-upload");
        p.setFeaturedFestival(req.featuredFestival());
        p.setActive(true);

        CraftProduct saved = repository.saveProduct(p);
        AuditEvent ev = new AuditEvent();
        ev.setActorUserId(req.artisanUserId());
        ev.setEventType("PRODUCT_CREATED");
        ev.setEntityType("PRODUCT");
        ev.setEntityId(saved.getSku());
        repository.saveAuditEvent(ev);
        fileService.appendAuditLog(ev);
        return saved;
    }

    @PostMapping("/artisan/upload-image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new InvalidInputException("file", "Image file cannot be empty");
        }
        String name = System.currentTimeMillis() + "-" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "craft.png");
        String publicUrl = repository.uploadProductImage(name, file.getBytes(), file.getContentType());
        return Map.of("imageUrl", publicUrl, "storageMode", repository.getBackendMode());
    }

    @GetMapping("/admin/artisans")
    public List<ArtisanProfile> listArtisans() {
        return repository.findAllArtisanProfiles();
    }

    @PatchMapping("/admin/artisans/{artisanId}/kyc")
    public ArtisanProfile decideKyc(@PathVariable UUID artisanId, @RequestBody KycDecisionRequest req)
            throws Exception {
        String st = req.kycStatus() != null ? req.kycStatus().trim().toUpperCase() : "";
        if (!List.of("VERIFIED", "REJECTED", "PENDING").contains(st)) {
            throw new InvalidInputException("kycStatus", "Must be VERIFIED, REJECTED, or PENDING");
        }
        ArtisanProfile updated = repository.updateArtisanKyc(artisanId, st);
        AuditEvent ev = new AuditEvent();
        ev.setEventType("KYC_STATUS_" + st);
        ev.setEntityType("ARTISAN_PROFILE");
        ev.setEntityId(artisanId.toString());
        repository.saveAuditEvent(ev);
        fileService.appendAuditLog(ev);
        return updated;
    }

    @PostMapping("/admin/concurrency-demo")
    public Map<String, Object> runConcurrencyDemo(@RequestBody ConcurrencyDemoRequest req) throws Exception {
        int threads = Math.min(Math.max(req.threads(), 2), 10);
        CraftProduct before = repository.findProductById(req.productId())
                .orElseThrow(() -> new InvalidInputException("productId", "Product not found"));
        int startStock = before.getStockQty();

        List<CompletableFuture<String>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int idx = i + 1;
            tasks.add(orderProcessor.submitOrderAsync(new ConcurrentOrderProcessor.CheckoutCommand(
                    req.customerId(),
                    List.of(new ConcurrentOrderProcessor.LineRequest(req.productId(), 1)),
                    "Concurrency Lab Demo Address #" + idx + ", Ahmedabad",
                    "UPI_TEST",
                    "concurrent" + idx + "@okweavo",
                    false,
                    true
            )).handle((order, err) -> {
                if (err == null) {
                    return "Thread-" + idx + ": SUCCESS (" + order.getOrderNumber() + ")";
                }
                Throwable cause = (err instanceof CompletionException && err.getCause() != null) ? err.getCause() : err;
                return "Thread-" + idx + ": BLOCKED (" + cause.getMessage() + ")";
            }));
        }

        List<String> outcomes = tasks.stream().map(CompletableFuture::join).toList();
        int afterStock = repository.findProductById(req.productId()).map(CraftProduct::getStockQty).orElse(0);
        long succeeded = outcomes.stream().filter(s -> s.contains("SUCCESS")).count();

        return Map.of(
                "threadsLaunched", threads,
                "stockBefore", startStock,
                "stockAfter", afterStock,
                "ordersSucceeded", succeeded,
                "oversellPrevented", afterStock >= 0,
                "outcomes", outcomes
        );
    }

    @GetMapping("/admin/notifications")
    public Map<String, Object> recentNotifications() {
        return Map.of(
                "workerThreadName", notificationWorker.getWorkerThreadName(),
                "processedCount", notificationWorker.getProcessedCount(),
                "pendingQueue", notificationWorker.getPendingCount(),
                "logs", repository.findRecentNotifications(20)
        );
    }

    @GetMapping("/admin/audit-logs")
    public Map<String, Object> recentAuditLogs() throws Exception {
        return Map.of(
                "supabaseEvents", repository.findRecentAuditEvents(20),
                "fileLogLines", fileService.readRecentAuditLines(20)
        );
    }

    @GetMapping(value = "/files/export/csv", produces = "text/csv")
    public String exportCatalogCsv() throws Exception {
        List<CraftProduct> products = repository.findActiveProducts();
        fileService.exportCatalogCsv(products, "weavo-catalog-latest.csv");
        return fileService.exportCatalogCsvAsString(products);
    }
}
