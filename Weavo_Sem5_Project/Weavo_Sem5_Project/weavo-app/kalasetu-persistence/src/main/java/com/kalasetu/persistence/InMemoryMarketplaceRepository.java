package com.kalasetu.persistence;

import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.model.*;
import com.kalasetu.domain.security.PasswordHasher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory repository used for unit/concurrency tests and offline fallback.
 */
public class InMemoryMarketplaceRepository implements MarketplaceRepository {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<UUID, ArtisanProfile> artisans = new ConcurrentHashMap<>();
    private final Map<UUID, CraftCategory> categories = new ConcurrentHashMap<>();
    private final Map<UUID, CraftProduct> products = new ConcurrentHashMap<>();
    private final Map<UUID, List<CartItem>> carts = new ConcurrentHashMap<>();
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();
    private final List<VerifiedReview> reviews = new CopyOnWriteArrayList<>();
    private final List<NotificationRecord> notifications = new CopyOnWriteArrayList<>();
    private final List<AuditEvent> audits = new CopyOnWriteArrayList<>();

    public InMemoryMarketplaceRepository() {
        seedDefaults();
    }

    private void seedDefaults() {
        UUID cat1 = UUID.fromString("11111111-1111-1111-1111-111111111101");
        UUID cat2 = UUID.fromString("11111111-1111-1111-1111-111111111102");
        UUID cat3 = UUID.fromString("11111111-1111-1111-1111-111111111103");
        categories.put(cat1, new CraftCategory(cat1, "Handloom & Textiles", "handloom-textiles", "Bandhani & Patola", "Gujarat"));
        categories.put(cat2, new CraftCategory(cat2, "Block Printing & Natural Dyes", "block-printing", "Ajrakh block print", "Kutch"));
        categories.put(cat3, new CraftCategory(cat3, "Pottery & Terracotta", "pottery-terracotta", "Gundiyali clay craft", "Kutch"));

        String hash = PasswordHasher.hash("Weavo@2026");
        UUID buyerId = UUID.fromString("22222222-2222-2222-2222-222222222201");
        UUID artisanUserId = UUID.fromString("22222222-2222-2222-2222-222222222203");
        UUID adminId = UUID.fromString("22222222-2222-2222-2222-222222222206");

        users.put(buyerId, new Customer(buyerId, "Aarav Patel", "aarav.buyer@weavo.demo", hash, "+91-9876500001", "Ahmedabad", "Gujarat", true));
        users.put(artisanUserId, new Artisan(artisanUserId, "Rameshbhai Khatri", "ramesh.artisan@weavo.demo", hash, "+91-9876500003", "Jamnagar", "Gujarat", true,
                "Khatri Bandhani & Ajrakh Kala Kendra", "Jamnagar Cluster", "GI-GJ-BANDHANI-2019", "VERIFIED", "KARIGAR_PRO"));
        users.put(adminId, new Admin(adminId, "Weavo Admin", "admin@weavo.demo", hash, "+91-9876500099", "Ahmedabad", "Gujarat", true));

        UUID artisanProfileId = UUID.fromString("33333333-3333-3333-3333-333333333301");
        ArtisanProfile ap = new ArtisanProfile();
        ap.setId(artisanProfileId);
        ap.setUserId(artisanUserId);
        ap.setWorkshopName("Khatri Bandhani & Ajrakh Kala Kendra");
        ap.setVillageCluster("Jamnagar Cluster");
        ap.setDistrict("Jamnagar");
        ap.setState("Gujarat");
        ap.setCraftSpecialty("Bandhani & Ajrakh");
        ap.setGiTagNumber("GI-GJ-BANDHANI-2019");
        ap.setKycStatus("VERIFIED");
        ap.setSubscriptionTier("KARIGAR_PRO");
        ap.setCommissionRate(new BigDecimal("8.00"));
        artisans.put(artisanProfileId, ap);

        CraftProduct p1 = new CraftProduct();
        p1.setId(UUID.fromString("44444444-4444-4444-4444-444444444401"));
        p1.setArtisanId(artisanProfileId);
        p1.setCategoryId(cat1);
        p1.setSku("WV-BN-001");
        p1.setTitle("Jamnagar Gajji Silk Bandhani Saree");
        p1.setCraftType("Bandhani Tie-Dye");
        p1.setOriginVillage("Jamnagar, Gujarat");
        p1.setGiCertified(true);
        p1.setDescription("Hand-knotted 14,000 Ekdali dots on Gajji silk.");
        p1.setPriceInr(new BigDecimal("4850.00"));
        p1.setStockQty(12);
        p1.setImageUrl("/images/bandhani-saree.png");
        p1.setFeaturedFestival(true);
        p1.setActive(true);
        products.put(p1.getId(), p1);

        CraftProduct p2 = new CraftProduct();
        p2.setId(UUID.fromString("44444444-4444-4444-4444-444444444402"));
        p2.setArtisanId(artisanProfileId);
        p2.setCategoryId(cat1);
        p2.setSku("WV-PT-002");
        p2.setTitle("Patan Double-Ikat Patola Silk Dupatta");
        p2.setCraftType("Patola Silk Weaving");
        p2.setOriginVillage("Patan, Gujarat");
        p2.setGiCertified(true);
        p2.setDescription("Geometric Navratna motifs woven on pure silk.");
        p2.setPriceInr(new BigDecimal("8500.00"));
        p2.setStockQty(6);
        p2.setImageUrl("/images/patola-dupatta.png");
        p2.setFeaturedFestival(true);
        p2.setActive(true);
        products.put(p2.getId(), p2);
    }

    @Override
    public String getBackendMode() {
        return "IN_MEMORY_FALLBACK";
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User createUser(User user) {
        if (user.getId() == null) user.setId(UUID.randomUUID());
        user.setCreatedAt(OffsetDateTime.now());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<ArtisanProfile> findAllArtisanProfiles() {
        return new ArrayList<>(artisans.values());
    }

    @Override
    public Optional<ArtisanProfile> findArtisanByUserId(UUID userId) {
        return artisans.values().stream().filter(a -> userId.equals(a.getUserId())).findFirst();
    }

    @Override
    public Optional<ArtisanProfile> findArtisanById(UUID artisanId) {
        return Optional.ofNullable(artisans.get(artisanId));
    }

    @Override
    public ArtisanProfile createArtisanProfile(ArtisanProfile profile) {
        if (profile.getId() == null) profile.setId(UUID.randomUUID());
        profile.setCreatedAt(OffsetDateTime.now());
        artisans.put(profile.getId(), profile);
        return profile;
    }

    @Override
    public ArtisanProfile updateArtisanKyc(UUID artisanId, String kycStatus) {
        ArtisanProfile ap = artisans.get(artisanId);
        if (ap != null) {
            ap.setKycStatus(kycStatus);
        }
        return ap;
    }

    @Override
    public List<CraftCategory> findAllCategories() {
        return new ArrayList<>(categories.values());
    }

    @Override
    public List<CraftProduct> findActiveProducts() {
        return products.values().stream().filter(CraftProduct::isActive).toList();
    }

    @Override
    public List<CraftProduct> searchProducts(String query, UUID categoryId, boolean giOnly) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return products.values().stream()
                .filter(CraftProduct::isActive)
                .filter(p -> q.isEmpty() || p.getTitle().toLowerCase().contains(q)
                        || p.getCraftType().toLowerCase().contains(q)
                        || p.getOriginVillage().toLowerCase().contains(q))
                .filter(p -> categoryId == null || categoryId.equals(p.getCategoryId()))
                .filter(p -> !giOnly || p.isGiCertified())
                .toList();
    }

    @Override
    public Optional<CraftProduct> findProductById(UUID productId) {
        return Optional.ofNullable(products.get(productId));
    }

    @Override
    public CraftProduct saveProduct(CraftProduct product) {
        if (product.getId() == null) product.setId(UUID.randomUUID());
        product.setCreatedAt(OffsetDateTime.now());
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public synchronized int decrementStockAtomic(UUID productId, int qty) throws OutOfStockException {
        CraftProduct p = products.get(productId);
        if (p == null || !p.isActive() || p.getStockQty() < qty) {
            throw new OutOfStockException(p != null ? p.getTitle() : productId.toString(),
                    p != null ? p.getStockQty() : 0, qty);
        }
        p.setStockQty(p.getStockQty() - qty);
        return p.getStockQty();
    }

    @Override
    public List<CartItem> findCartByCustomer(UUID customerId) {
        return new ArrayList<>(carts.getOrDefault(customerId, List.of()));
    }

    @Override
    public CartItem upsertCartItem(UUID customerId, UUID productId, int quantity) {
        List<CartItem> items = carts.computeIfAbsent(customerId, k -> new CopyOnWriteArrayList<>());
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return item;
            }
        }
        CartItem created = new CartItem();
        created.setId(UUID.randomUUID());
        created.setCustomerId(customerId);
        created.setProductId(productId);
        created.setQuantity(quantity);
        created.setAddedAt(OffsetDateTime.now());
        items.add(created);
        return created;
    }

    @Override
    public void clearCart(UUID customerId) {
        carts.remove(customerId);
    }

    @Override
    public void removeCartItem(UUID customerId, UUID productId) {
        List<CartItem> items = carts.get(customerId);
        if (items != null) items.removeIf(i -> i.getProductId().equals(productId));
    }

    @Override
    public Order saveOrder(Order order) {
        if (order.getId() == null) order.setId(UUID.randomUUID());
        if (order.getCreatedAt() == null) order.setCreatedAt(OffsetDateTime.now());
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public List<Order> findOrdersByCustomer(UUID customerId) {
        return orders.values().stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Order> findAllOrders() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public Optional<Order> findOrderById(UUID orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public Order updateOrderStatus(UUID orderId, String orderStatus) {
        Order o = orders.get(orderId);
        if (o != null) o.setOrderStatus(orderStatus);
        return o;
    }

    @Override
    public List<VerifiedReview> findReviewsByProduct(UUID productId) {
        return reviews.stream().filter(r -> productId.equals(r.getProductId())).toList();
    }

    @Override
    public VerifiedReview saveReview(VerifiedReview review) {
        if (review.getId() == null) review.setId(UUID.randomUUID());
        review.setCreatedAt(OffsetDateTime.now());
        reviews.add(review);
        return review;
    }

    @Override
    public NotificationRecord saveNotification(NotificationRecord record) {
        if (record.getId() == null) record.setId(UUID.randomUUID());
        record.setCreatedAt(OffsetDateTime.now());
        notifications.add(record);
        return record;
    }

    @Override
    public List<NotificationRecord> findRecentNotifications(int limit) {
        int start = Math.max(0, notifications.size() - limit);
        List<NotificationRecord> sub = new ArrayList<>(notifications.subList(start, notifications.size()));
        Collections.reverse(sub);
        return sub;
    }

    @Override
    public AuditEvent saveAuditEvent(AuditEvent event) {
        if (event.getId() == null) event.setId(UUID.randomUUID());
        event.setCreatedAt(OffsetDateTime.now());
        audits.add(event);
        return event;
    }

    @Override
    public List<AuditEvent> findRecentAuditEvents(int limit) {
        int start = Math.max(0, audits.size() - limit);
        List<AuditEvent> sub = new ArrayList<>(audits.subList(start, audits.size()));
        Collections.reverse(sub);
        return sub;
    }

    @Override
    public String uploadProductImage(String filename, byte[] bytes, String contentType) {
        return "/images/" + filename;
    }
}
