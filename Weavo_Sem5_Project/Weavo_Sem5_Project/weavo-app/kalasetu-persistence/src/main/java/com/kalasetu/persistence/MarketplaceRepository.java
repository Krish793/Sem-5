package com.kalasetu.persistence;

import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketplaceRepository {
    String getBackendMode();

    // Users & Artisans
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserById(UUID id);
    User createUser(User user);
    List<ArtisanProfile> findAllArtisanProfiles();
    Optional<ArtisanProfile> findArtisanByUserId(UUID userId);
    Optional<ArtisanProfile> findArtisanById(UUID artisanId);
    ArtisanProfile createArtisanProfile(ArtisanProfile profile);
    ArtisanProfile updateArtisanKyc(UUID artisanId, String kycStatus);

    // Categories & Products
    List<CraftCategory> findAllCategories();
    List<CraftProduct> findActiveProducts();
    List<CraftProduct> searchProducts(String query, UUID categoryId, boolean giOnly);
    Optional<CraftProduct> findProductById(UUID productId);
    CraftProduct saveProduct(CraftProduct product);
    int decrementStockAtomic(UUID productId, int qty) throws OutOfStockException;

    // Cart
    List<CartItem> findCartByCustomer(UUID customerId);
    CartItem upsertCartItem(UUID customerId, UUID productId, int quantity);
    void clearCart(UUID customerId);
    void removeCartItem(UUID customerId, UUID productId);

    // Orders
    Order saveOrder(Order order);
    List<Order> findOrdersByCustomer(UUID customerId);
    List<Order> findAllOrders();
    Optional<Order> findOrderById(UUID orderId);
    Order updateOrderStatus(UUID orderId, String orderStatus);

    // Verified Reviews
    List<VerifiedReview> findReviewsByProduct(UUID productId);
    VerifiedReview saveReview(VerifiedReview review);

    // Notifications & Audit
    NotificationRecord saveNotification(NotificationRecord record);
    List<NotificationRecord> findRecentNotifications(int limit);
    AuditEvent saveAuditEvent(AuditEvent event);
    List<AuditEvent> findRecentAuditEvents(int limit);

    // Storage
    String uploadProductImage(String filename, byte[] bytes, String contentType);
}
