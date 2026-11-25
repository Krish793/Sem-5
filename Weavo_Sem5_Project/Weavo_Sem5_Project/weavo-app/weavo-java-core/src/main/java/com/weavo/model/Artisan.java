package com.weavo.model;

/**
 * Artisan class representing rural craftspeople and weavers on Weavo.
 * Demonstrates Inheritance from abstract User class.
 */
public class Artisan extends User {
    private String craftClusterVillage;
    private String craftSpecialty;
    private boolean giCertified;
    private double directPayoutPercentage; // Default 92%

    public Artisan(String id, String fullName, String email, String city, String state, 
                   boolean dpdpConsentAccepted, String craftClusterVillage, String craftSpecialty, boolean giCertified) {
        super(id, fullName, email, city, state, dpdpConsentAccepted);
        this.craftClusterVillage = craftClusterVillage;
        this.craftSpecialty = craftSpecialty;
        this.giCertified = giCertified;
        this.directPayoutPercentage = 92.0; // 92% direct karigar cut
    }

    @Override
    public String getRoleName() {
        return "ARTISAN";
    }

    @Override
    public boolean canManageInventory() {
        return true;
    }

    @Override
    public boolean canPlaceOrders() {
        return false;
    }

    public String getCraftClusterVillage() { return craftClusterVillage; }
    public String getCraftSpecialty() { return craftSpecialty; }
    public boolean isGiCertified() { return giCertified; }
    public double getDirectPayoutPercentage() { return directPayoutPercentage; }

    public double calculateArtisanEarnings(double retailPrice) {
        return (retailPrice * directPayoutPercentage) / 100.0;
    }
}
