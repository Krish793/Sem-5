package com.kalasetu.domain.model;

import java.util.List;
import java.util.UUID;

public class Artisan extends User {
    private String workshopName;
    private String villageCluster;
    private String giTagNumber;
    private String kycStatus = "PENDING";
    private String subscriptionTier = "STARTER_FREE";

    public Artisan() {}

    public Artisan(UUID id, String fullName, String email, String passwordHash,
                   String phone, String city, String state, boolean dpdpConsent,
                   String workshopName, String villageCluster, String giTagNumber,
                   String kycStatus, String subscriptionTier) {
        super(id, fullName, email, passwordHash, phone, city, state, dpdpConsent);
        this.workshopName = workshopName;
        this.villageCluster = villageCluster;
        this.giTagNumber = giTagNumber;
        this.kycStatus = kycStatus != null ? kycStatus : "PENDING";
        this.subscriptionTier = subscriptionTier != null ? subscriptionTier : "STARTER_FREE";
    }

    @Override
    public Role getRole() {
        return Role.ARTISAN;
    }

    @Override
    public List<String> getDashboardCapabilities() {
        return List.of("LIST_CRAFTS", "UPLOAD_IMAGES", "EXPORT_CSV_CATALOG", "VIEW_PAYOUTS");
    }

    /**
     * Commission policy: 8% for GI-certified or Karigar Pro/Co-op tiers, 10% standard.
     */
    @Override
    public double calculatePlatformFeeRate() {
        if ("KARIGAR_PRO".equalsIgnoreCase(subscriptionTier)
                || "COOP_STUDIO".equalsIgnoreCase(subscriptionTier)
                || (giTagNumber != null && !giTagNumber.isBlank())) {
            return 0.08;
        }
        return 0.10;
    }

    @Override
    public boolean canListCraft() {
        return "VERIFIED".equalsIgnoreCase(kycStatus);
    }

    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }

    public String getVillageCluster() { return villageCluster; }
    public void setVillageCluster(String villageCluster) { this.villageCluster = villageCluster; }

    public String getGiTagNumber() { return giTagNumber; }
    public void setGiTagNumber(String giTagNumber) { this.giTagNumber = giTagNumber; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }
}
