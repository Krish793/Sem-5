package com.kalasetu.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ArtisanProfile {
    private UUID id;
    private UUID userId;
    private String workshopName;
    private String villageCluster;
    private String district;
    private String state;
    private String craftSpecialty;
    private String giTagNumber;
    private String kycStatus;
    private String subscriptionTier;
    private BigDecimal commissionRate;
    private String bio;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }

    public String getVillageCluster() { return villageCluster; }
    public void setVillageCluster(String villageCluster) { this.villageCluster = villageCluster; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCraftSpecialty() { return craftSpecialty; }
    public void setCraftSpecialty(String craftSpecialty) { this.craftSpecialty = craftSpecialty; }

    public String getGiTagNumber() { return giTagNumber; }
    public void setGiTagNumber(String giTagNumber) { this.giTagNumber = giTagNumber; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
