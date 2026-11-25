package com.kalasetu.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.model.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Live Supabase Cloud Repository using PostgREST, PostgreSQL RPC, and Supabase Storage APIs over HTTPS.
 */
public class SupabaseRestRepository implements MarketplaceRepository {
    private final String supabaseUrl;
    private final String serviceKey;
    private final String storageBucket;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public SupabaseRestRepository(String supabaseUrl, String serviceKey, String storageBucket) {
        this.supabaseUrl = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        this.serviceKey = serviceKey;
        this.storageBucket = (storageBucket == null || storageBucket.isBlank()) ? "product-images" : storageBucket;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String getBackendMode() {
        return "SUPABASE_CLOUD_POSTGRES";
    }

    private JsonNode requestJson(String method, String pathAndQuery, Object body, String preferHeader) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + pathAndQuery))
                    .timeout(Duration.ofSeconds(15))
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", "application/json");
            if (preferHeader != null) {
                b.header("Prefer", preferHeader);
            }
            String jsonBody = body == null ? "" : mapper.writeValueAsString(body);
            HttpRequest.BodyPublisher pub = body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8);

            HttpResponse<String> resp = httpClient.send(b.method(method, pub).build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new IllegalStateException("Supabase HTTP " + resp.statusCode() + ": " + resp.body());
            }
            if (resp.body() == null || resp.body().isBlank()) {
                return mapper.createArrayNode();
            }
            return mapper.readTree(resp.body());
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("Supabase request error on " + pathAndQuery + ": " + e.getMessage(), e);
        }
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    // ==================== USERS & ARTISANS ====================

    @Override
    public Optional<User> findUserByEmail(String email) {
        JsonNode arr = requestJson("GET", "/rest/v1/app_users?email=eq." + enc(email.toLowerCase()) + "&select=*", null, null);
        if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
        return Optional.of(mapUser(arr.get(0)));
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        JsonNode arr = requestJson("GET", "/rest/v1/app_users?id=eq." + id + "&select=*", null, null);
        if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
        return Optional.of(mapUser(arr.get(0)));
    }

    @Override
    public User createUser(User user) {
        ObjectNode node = mapper.createObjectNode();
        if (user.getId() != null) node.put("id", user.getId().toString());
        node.put("full_name", user.getFullName());
        node.put("email", user.getEmail().toLowerCase());
        node.put("password_hash", user.getPasswordHash());
        node.put("role", user.getRole().name());
        node.put("phone", user.getPhone());
        node.put("city", user.getCity());
        node.put("state", user.getState() != null ? user.getState() : "Gujarat");
        node.put("dpdp_consent", user.isDpdpConsent());
        node.put("is_active", user.isActive());

        JsonNode res = requestJson("POST", "/rest/v1/app_users", node, "return=representation");
        return mapUser(res.get(0));
    }

    private User mapUser(JsonNode n) {
        UUID id = UUID.fromString(n.get("id").asText());
        String role = n.get("role").asText("CUSTOMER");
        String fullName = n.path("full_name").asText("");
        String email = n.path("email").asText("");
        String passHash = n.path("password_hash").asText("");
        String phone = n.path("phone").asText("");
        String city = n.path("city").asText("");
        String state = n.path("state").asText("Gujarat");
        boolean consent = n.path("dpdp_consent").asBoolean(true);

        User u;
        if ("ARTISAN".equalsIgnoreCase(role)) {
            Optional<ArtisanProfile> ap = findArtisanByUserId(id);
            u = new Artisan(id, fullName, email, passHash, phone, city, state, consent,
                    ap.map(ArtisanProfile::getWorkshopName).orElse(fullName + " Workshop"),
                    ap.map(ArtisanProfile::getVillageCluster).orElse(city),
                    ap.map(ArtisanProfile::getGiTagNumber).orElse(null),
                    ap.map(ArtisanProfile::getKycStatus).orElse("PENDING"),
                    ap.map(ArtisanProfile::getSubscriptionTier).orElse("STARTER_FREE"));
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            u = new Admin(id, fullName, email, passHash, phone, city, state, consent);
        } else {
            u = new Customer(id, fullName, email, passHash, phone, city, state, consent);
        }
        if (n.hasNonNull("created_at")) {
            u.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        }
        return u;
    }

    @Override
    public List<ArtisanProfile> findAllArtisanProfiles() {
        JsonNode arr = requestJson("GET", "/rest/v1/artisan_profiles?select=*&order=created_at.desc", null, null);
        List<ArtisanProfile> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapArtisan(n));
        return list;
    }

    @Override
    public Optional<ArtisanProfile> findArtisanByUserId(UUID userId) {
        JsonNode arr = requestJson("GET", "/rest/v1/artisan_profiles?user_id=eq." + userId + "&select=*", null, null);
        if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
        return Optional.of(mapArtisan(arr.get(0)));
    }

    @Override
    public Optional<ArtisanProfile> findArtisanById(UUID artisanId) {
        JsonNode arr = requestJson("GET", "/rest/v1/artisan_profiles?id=eq." + artisanId + "&select=*", null, null);
        if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
        return Optional.of(mapArtisan(arr.get(0)));
    }

    @Override
    public ArtisanProfile createArtisanProfile(ArtisanProfile p) {
        ObjectNode node = mapper.createObjectNode();
        node.put("user_id", p.getUserId().toString());
        node.put("workshop_name", p.getWorkshopName());
        node.put("village_cluster", p.getVillageCluster());
        node.put("district", p.getDistrict());
        node.put("state", p.getState() != null ? p.getState() : "Gujarat");
        node.put("craft_specialty", p.getCraftSpecialty());
        if (p.getGiTagNumber() != null) node.put("gi_tag_number", p.getGiTagNumber());
        node.put("kyc_status", p.getKycStatus() != null ? p.getKycStatus() : "PENDING");
        node.put("subscription_tier", p.getSubscriptionTier() != null ? p.getSubscriptionTier() : "STARTER_FREE");
        node.put("commission_rate", p.getCommissionRate() != null ? p.getCommissionRate() : new BigDecimal("10.00"));
        node.put("bio", p.getBio() != null ? p.getBio() : "");
        JsonNode res = requestJson("POST", "/rest/v1/artisan_profiles", node, "return=representation");
        return mapArtisan(res.get(0));
    }

    @Override
    public ArtisanProfile updateArtisanKyc(UUID artisanId, String kycStatus) {
        ObjectNode node = mapper.createObjectNode();
        node.put("kyc_status", kycStatus);
        JsonNode res = requestJson("PATCH", "/rest/v1/artisan_profiles?id=eq." + artisanId, node, "return=representation");
        return res.isArray() && !res.isEmpty() ? mapArtisan(res.get(0)) : null;
    }

    private ArtisanProfile mapArtisan(JsonNode n) {
        ArtisanProfile ap = new ArtisanProfile();
        ap.setId(UUID.fromString(n.get("id").asText()));
        ap.setUserId(UUID.fromString(n.get("user_id").asText()));
        ap.setWorkshopName(n.path("workshop_name").asText(""));
        ap.setVillageCluster(n.path("village_cluster").asText(""));
        ap.setDistrict(n.path("district").asText(""));
        ap.setState(n.path("state").asText("Gujarat"));
        ap.setCraftSpecialty(n.path("craft_specialty").asText(""));
        ap.setGiTagNumber(n.hasNonNull("gi_tag_number") ? n.get("gi_tag_number").asText() : null);
        ap.setKycStatus(n.path("kyc_status").asText("PENDING"));
        ap.setSubscriptionTier(n.path("subscription_tier").asText("STARTER_FREE"));
        ap.setCommissionRate(new BigDecimal(n.path("commission_rate").asText("10.00")));
        ap.setBio(n.path("bio").asText(""));
        if (n.hasNonNull("created_at")) ap.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        return ap;
    }

    // ==================== CATEGORIES & PRODUCTS ====================

    @Override
    public List<CraftCategory> findAllCategories() {
        JsonNode arr = requestJson("GET", "/rest/v1/craft_categories?select=*&order=name.asc", null, null);
        List<CraftCategory> list = new ArrayList<>();
        for (JsonNode n : arr) {
            list.add(new CraftCategory(
                    UUID.fromString(n.get("id").asText()),
                    n.path("name").asText(),
                    n.path("slug").asText(),
                    n.path("description").asText(""),
                    n.path("region_focus").asText("")
            ));
        }
        return list;
    }

    @Override
    public List<CraftProduct> findActiveProducts() {
        JsonNode arr = requestJson("GET", "/rest/v1/products?is_active=eq.true&select=*&order=is_featured_festival.desc,created_at.desc", null, null);
        List<CraftProduct> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapProduct(n));
        return list;
    }

    @Override
    public List<CraftProduct> searchProducts(String query, UUID categoryId, boolean giOnly) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return findActiveProducts().stream()
                .filter(p -> q.isEmpty() || p.getTitle().toLowerCase().contains(q)
                        || p.getCraftType().toLowerCase().contains(q)
                        || p.getOriginVillage().toLowerCase().contains(q))
                .filter(p -> categoryId == null || categoryId.equals(p.getCategoryId()))
                .filter(p -> !giOnly || p.isGiCertified())
                .toList();
    }

    @Override
    public Optional<CraftProduct> findProductById(UUID productId) {
        JsonNode arr = requestJson("GET", "/rest/v1/products?id=eq." + productId + "&select=*", null, null);
        if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
        return Optional.of(mapProduct(arr.get(0)));
    }

    @Override
    public CraftProduct saveProduct(CraftProduct p) {
        ObjectNode node = mapper.createObjectNode();
        node.put("artisan_id", p.getArtisanId().toString());
        if (p.getCategoryId() != null) node.put("category_id", p.getCategoryId().toString());
        node.put("sku", p.getSku());
        node.put("title", p.getTitle());
        node.put("craft_type", p.getCraftType());
        node.put("origin_village", p.getOriginVillage());
        node.put("gi_certified", p.isGiCertified());
        node.put("description", p.getDescription());
        node.put("price_inr", p.getPriceInr());
        node.put("stock_qty", p.getStockQty());
        node.put("image_url", p.getImageUrl());
        node.put("storage_path", p.getStoragePath());
        node.put("is_featured_festival", p.isFeaturedFestival());
        node.put("is_active", p.isActive());

        JsonNode res = requestJson("POST", "/rest/v1/products", node, "return=representation");
        return mapProduct(res.get(0));
    }

    @Override
    public int decrementStockAtomic(UUID productId, int qty) throws OutOfStockException {
        Optional<CraftProduct> current = findProductById(productId);
        ObjectNode body = mapper.createObjectNode();
        body.put("p_product_id", productId.toString());
        body.put("p_qty", qty);
        try {
            JsonNode res = requestJson("POST", "/rest/v1/rpc/decrement_product_stock", body, null);
            return res.asInt();
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("OUT_OF_STOCK")) {
                throw new OutOfStockException(
                        current.map(CraftProduct::getTitle).orElse(productId.toString()),
                        current.map(CraftProduct::getStockQty).orElse(0),
                        qty
                );
            }
            throw ex;
        }
    }

    private CraftProduct mapProduct(JsonNode n) {
        CraftProduct p = new CraftProduct();
        p.setId(UUID.fromString(n.get("id").asText()));
        p.setArtisanId(UUID.fromString(n.get("artisan_id").asText()));
        if (n.hasNonNull("category_id")) p.setCategoryId(UUID.fromString(n.get("category_id").asText()));
        p.setSku(n.path("sku").asText());
        p.setTitle(n.path("title").asText());
        p.setCraftType(n.path("craft_type").asText());
        p.setOriginVillage(n.path("origin_village").asText());
        p.setGiCertified(n.path("gi_certified").asBoolean(false));
        p.setDescription(n.path("description").asText());
        p.setPriceInr(new BigDecimal(n.path("price_inr").asText("0.00")));
        p.setStockQty(n.path("stock_qty").asInt(0));
        p.setImageUrl(n.path("image_url").asText(""));
        p.setStoragePath(n.path("storage_path").asText(""));
        p.setFeaturedFestival(n.path("is_featured_festival").asBoolean(false));
        p.setActive(n.path("is_active").asBoolean(true));
        if (n.hasNonNull("created_at")) p.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        return p;
    }

    // ==================== CART ====================

    @Override
    public List<CartItem> findCartByCustomer(UUID customerId) {
        JsonNode arr = requestJson("GET", "/rest/v1/cart_items?customer_id=eq." + customerId + "&select=*", null, null);
        List<CartItem> items = new ArrayList<>();
        for (JsonNode n : arr) items.add(mapCartItem(n));
        return items;
    }

    @Override
    public CartItem upsertCartItem(UUID customerId, UUID productId, int quantity) {
        ObjectNode node = mapper.createObjectNode();
        node.put("customer_id", customerId.toString());
        node.put("product_id", productId.toString());
        node.put("quantity", quantity);
        JsonNode res = requestJson("POST", "/rest/v1/cart_items?on_conflict=customer_id,product_id",
                node, "resolution=merge-duplicates,return=representation");
        return mapCartItem(res.get(0));
    }

    @Override
    public void clearCart(UUID customerId) {
        requestJson("DELETE", "/rest/v1/cart_items?customer_id=eq." + customerId, null, null);
    }

    @Override
    public void removeCartItem(UUID customerId, UUID productId) {
        requestJson("DELETE", "/rest/v1/cart_items?customer_id=eq." + customerId + "&product_id=eq." + productId, null, null);
    }

    private CartItem mapCartItem(JsonNode n) {
        CartItem ci = new CartItem();
        ci.setId(UUID.fromString(n.get("id").asText()));
        ci.setCustomerId(UUID.fromString(n.get("customer_id").asText()));
        ci.setProductId(UUID.fromString(n.get("product_id").asText()));
        ci.setQuantity(n.path("quantity").asInt(1));
        if (n.hasNonNull("added_at")) ci.setAddedAt(OffsetDateTime.parse(n.get("added_at").asText()));
        return ci;
    }

    // ==================== ORDERS ====================

    @Override
    public Order saveOrder(Order o) {
        ObjectNode node = mapper.createObjectNode();
        if (o.getId() != null) node.put("id", o.getId().toString());
        node.put("order_number", o.getOrderNumber());
        node.put("customer_id", o.getCustomerId().toString());
        node.put("total_amount_inr", o.getTotalAmountInr());
        node.put("platform_commission_inr", o.getPlatformCommissionInr());
        node.put("shipping_fee_inr", o.getShippingFeeInr());
        node.put("payment_method", o.getPaymentMethod());
        node.put("payment_status", o.getPaymentStatus());
        node.put("transaction_ref", o.getTransactionRef());
        node.put("order_status", o.getOrderStatus());
        node.put("shipping_address", o.getShippingAddress());
        node.put("dpdp_consent_version", o.getDpdpConsentVersion());

        JsonNode res = requestJson("POST", "/rest/v1/orders", node, "return=representation");
        Order saved = mapOrder(res.get(0));

        List<OrderItem> savedItems = new ArrayList<>();
        for (OrderItem item : o.getItems()) {
            ObjectNode in = mapper.createObjectNode();
            in.put("order_id", saved.getId().toString());
            in.put("product_id", item.getProductId().toString());
            in.put("artisan_id", item.getArtisanId().toString());
            in.put("quantity", item.getQuantity());
            in.put("unit_price_inr", item.getUnitPriceInr());
            in.put("line_total_inr", item.getLineTotalInr());
            in.put("artisan_payout_inr", item.getArtisanPayoutInr());
            JsonNode itemRes = requestJson("POST", "/rest/v1/order_items", in, "return=representation");
            OrderItem mapped = mapOrderItem(itemRes.get(0));
            mapped.setProductTitle(item.getProductTitle());
            savedItems.add(mapped);
        }
        saved.setItems(savedItems);
        return saved;
    }

    @Override
    public List<Order> findOrdersByCustomer(UUID customerId) {
        JsonNode arr = requestJson("GET", "/rest/v1/orders?customer_id=eq." + customerId + "&select=*,order_items(*)&order=created_at.desc", null, null);
        List<Order> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapOrder(n));
        return list;
    }

    @Override
    public List<Order> findAllOrders() {
        JsonNode arr = requestJson("GET", "/rest/v1/orders?select=*,order_items(*)&order=created_at.desc", null, null);
        List<Order> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapOrder(n));
        return list;
    }

    @Override
    public Optional<Order> findOrderById(UUID orderId) {
        JsonNode arr = requestJson("GET", "/rest/v1/orders?id=eq." + orderId + "&select=*,order_items(*)", null, null);
        if (!arr.isArray() || arr.isEmpty()) return Optional.empty();
        return Optional.of(mapOrder(arr.get(0)));
    }

    @Override
    public Order updateOrderStatus(UUID orderId, String orderStatus) {
        ObjectNode node = mapper.createObjectNode();
        node.put("order_status", orderStatus);
        JsonNode res = requestJson("PATCH", "/rest/v1/orders?id=eq." + orderId, node, "return=representation");
        return res.isArray() && !res.isEmpty() ? mapOrder(res.get(0)) : null;
    }

    private Order mapOrder(JsonNode n) {
        Order o = new Order();
        o.setId(UUID.fromString(n.get("id").asText()));
        o.setOrderNumber(n.path("order_number").asText());
        o.setCustomerId(UUID.fromString(n.get("customer_id").asText()));
        o.setTotalAmountInr(new BigDecimal(n.path("total_amount_inr").asText("0")));
        o.setPlatformCommissionInr(new BigDecimal(n.path("platform_commission_inr").asText("0")));
        o.setShippingFeeInr(new BigDecimal(n.path("shipping_fee_inr").asText("0")));
        o.setPaymentMethod(n.path("payment_method").asText());
        o.setPaymentStatus(n.path("payment_status").asText());
        o.setTransactionRef(n.path("transaction_ref").asText());
        o.setOrderStatus(n.path("order_status").asText());
        o.setShippingAddress(n.path("shipping_address").asText());
        o.setDpdpConsentVersion(n.path("dpdp_consent_version").asText("v1.0-2026"));
        if (n.hasNonNull("created_at")) o.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        if (n.has("order_items") && n.get("order_items").isArray()) {
            List<OrderItem> items = new ArrayList<>();
            for (JsonNode in : n.get("order_items")) items.add(mapOrderItem(in));
            o.setItems(items);
        }
        return o;
    }

    private OrderItem mapOrderItem(JsonNode n) {
        OrderItem oi = new OrderItem();
        oi.setId(UUID.fromString(n.get("id").asText()));
        oi.setOrderId(UUID.fromString(n.get("order_id").asText()));
        oi.setProductId(UUID.fromString(n.get("product_id").asText()));
        oi.setArtisanId(UUID.fromString(n.get("artisan_id").asText()));
        oi.setQuantity(n.path("quantity").asInt(1));
        oi.setUnitPriceInr(new BigDecimal(n.path("unit_price_inr").asText("0")));
        oi.setLineTotalInr(new BigDecimal(n.path("line_total_inr").asText("0")));
        oi.setArtisanPayoutInr(new BigDecimal(n.path("artisan_payout_inr").asText("0")));
        return oi;
    }

    // ==================== VERIFIED REVIEWS ====================

    @Override
    public List<VerifiedReview> findReviewsByProduct(UUID productId) {
        JsonNode arr = requestJson("GET", "/rest/v1/verified_reviews?product_id=eq." + productId + "&select=*&order=created_at.desc", null, null);
        List<VerifiedReview> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapReview(n));
        return list;
    }

    @Override
    public VerifiedReview saveReview(VerifiedReview r) {
        ObjectNode node = mapper.createObjectNode();
        node.put("product_id", r.getProductId().toString());
        node.put("customer_id", r.getCustomerId().toString());
        node.put("order_id", r.getOrderId().toString());
        node.put("rating", r.getRating());
        node.put("comment", r.getComment());
        node.put("is_verified_purchase", r.isVerifiedPurchase());
        node.put("moderation_status", r.getModerationStatus());
        JsonNode res = requestJson("POST", "/rest/v1/verified_reviews", node, "return=representation");
        return mapReview(res.get(0));
    }

    private VerifiedReview mapReview(JsonNode n) {
        VerifiedReview r = new VerifiedReview();
        r.setId(UUID.fromString(n.get("id").asText()));
        r.setProductId(UUID.fromString(n.get("product_id").asText()));
        r.setCustomerId(UUID.fromString(n.get("customer_id").asText()));
        r.setOrderId(UUID.fromString(n.get("order_id").asText()));
        r.setRating(n.path("rating").asInt(5));
        r.setComment(n.path("comment").asText(""));
        r.setVerifiedPurchase(n.path("is_verified_purchase").asBoolean(true));
        r.setModerationStatus(n.path("moderation_status").asText("APPROVED"));
        if (n.hasNonNull("created_at")) r.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        return r;
    }

    // ==================== NOTIFICATIONS & AUDIT ====================

    @Override
    public NotificationRecord saveNotification(NotificationRecord nr) {
        ObjectNode node = mapper.createObjectNode();
        if (nr.getOrderId() != null) node.put("order_id", nr.getOrderId().toString());
        if (nr.getRecipientUserId() != null) node.put("recipient_user_id", nr.getRecipientUserId().toString());
        node.put("channel", nr.getChannel());
        node.put("message", nr.getMessage());
        node.put("status", nr.getStatus());
        node.put("worker_thread_name", nr.getWorkerThreadName());
        JsonNode res = requestJson("POST", "/rest/v1/notification_logs", node, "return=representation");
        return mapNotification(res.get(0));
    }

    @Override
    public List<NotificationRecord> findRecentNotifications(int limit) {
        JsonNode arr = requestJson("GET", "/rest/v1/notification_logs?select=*&order=created_at.desc&limit=" + limit, null, null);
        List<NotificationRecord> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapNotification(n));
        return list;
    }

    private NotificationRecord mapNotification(JsonNode n) {
        NotificationRecord r = new NotificationRecord();
        r.setId(UUID.fromString(n.get("id").asText()));
        if (n.hasNonNull("order_id")) r.setOrderId(UUID.fromString(n.get("order_id").asText()));
        if (n.hasNonNull("recipient_user_id")) r.setRecipientUserId(UUID.fromString(n.get("recipient_user_id").asText()));
        r.setChannel(n.path("channel").asText());
        r.setMessage(n.path("message").asText());
        r.setStatus(n.path("status").asText());
        r.setWorkerThreadName(n.path("worker_thread_name").asText());
        if (n.hasNonNull("created_at")) r.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        return r;
    }

    @Override
    public AuditEvent saveAuditEvent(AuditEvent e) {
        ObjectNode node = mapper.createObjectNode();
        if (e.getActorUserId() != null) node.put("actor_user_id", e.getActorUserId().toString());
        node.put("event_type", e.getEventType());
        node.put("entity_type", e.getEntityType());
        node.put("entity_id", e.getEntityId());
        if (e.getDetails() != null) node.set("details", mapper.valueToTree(e.getDetails()));
        JsonNode res = requestJson("POST", "/rest/v1/audit_events", node, "return=representation");
        return mapAudit(res.get(0));
    }

    @Override
    public List<AuditEvent> findRecentAuditEvents(int limit) {
        JsonNode arr = requestJson("GET", "/rest/v1/audit_events?select=*&order=created_at.desc&limit=" + limit, null, null);
        List<AuditEvent> list = new ArrayList<>();
        for (JsonNode n : arr) list.add(mapAudit(n));
        return list;
    }

    private AuditEvent mapAudit(JsonNode n) {
        AuditEvent e = new AuditEvent();
        e.setId(UUID.fromString(n.get("id").asText()));
        if (n.hasNonNull("actor_user_id")) e.setActorUserId(UUID.fromString(n.get("actor_user_id").asText()));
        e.setEventType(n.path("event_type").asText());
        e.setEntityType(n.path("entity_type").asText());
        e.setEntityId(n.path("entity_id").asText());
        if (n.hasNonNull("created_at")) e.setCreatedAt(OffsetDateTime.parse(n.get("created_at").asText()));
        return e;
    }

    // ==================== SUPABASE STORAGE UPLOAD ====================

    @Override
    public String uploadProductImage(String filename, byte[] bytes, String contentType) {
        String safeName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + storageBucket + "/" + safeName))
                    .timeout(Duration.ofSeconds(20))
                    .header("apikey", serviceKey)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", contentType != null ? contentType : "image/png")
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new IllegalStateException("Supabase Storage upload failed HTTP " + resp.statusCode() + ": " + resp.body());
            }
            return supabaseUrl + "/storage/v1/object/public/" + storageBucket + "/" + safeName;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("Storage upload failed: " + e.getMessage(), e);
        }
    }
}
