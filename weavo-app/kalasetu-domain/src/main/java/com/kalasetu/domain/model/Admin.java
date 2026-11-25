package com.kalasetu.domain.model;

import java.util.List;
import java.util.UUID;

public class Admin extends User {
    public Admin() {}

    public Admin(UUID id, String fullName, String email, String passwordHash,
                 String phone, String city, String state, boolean dpdpConsent) {
        super(id, fullName, email, passwordHash, phone, city, state, dpdpConsent);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public List<String> getDashboardCapabilities() {
        return List.of("APPROVE_ARTISAN_KYC", "VERIFY_GI_TAGS", "MODERATE_REVIEWS", "VIEW_AUDIT_LOGS", "STRESS_TEST_CONCURRENCY");
    }

    @Override
    public double calculatePlatformFeeRate() {
        return 0.0;
    }

    @Override
    public boolean canListCraft() {
        return true;
    }
}
