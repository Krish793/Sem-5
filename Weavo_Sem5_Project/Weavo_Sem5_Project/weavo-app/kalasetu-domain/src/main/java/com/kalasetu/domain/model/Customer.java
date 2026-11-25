package com.kalasetu.domain.model;

import java.util.List;
import java.util.UUID;

public class Customer extends User {
    public Customer() {}

    public Customer(UUID id, String fullName, String email, String passwordHash,
                    String phone, String city, String state, boolean dpdpConsent) {
        super(id, fullName, email, passwordHash, phone, city, state, dpdpConsent);
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    @Override
    public List<String> getDashboardCapabilities() {
        return List.of("BROWSE_CRAFTS", "CART_CHECKOUT_TEST", "TRACK_ORDERS", "VERIFIED_REVIEWS");
    }

    @Override
    public double calculatePlatformFeeRate() {
        return 0.0; // Buyers pay zero marketplace surcharge (transparent pricing, no dark patterns)
    }
}
