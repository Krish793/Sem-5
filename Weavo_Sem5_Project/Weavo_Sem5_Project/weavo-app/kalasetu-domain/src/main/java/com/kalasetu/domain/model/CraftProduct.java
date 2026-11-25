package com.kalasetu.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CraftProduct {
    private UUID id;
    private UUID artisanId;
    private UUID categoryId;
    private String sku;
    private String title;
    private String craftType;
    private String originVillage;
    private boolean giCertified;
    private String description;
    private BigDecimal priceInr;
    private int stockQty;
    private String imageUrl;
    private String storagePath;
    private boolean featuredFestival;
    private boolean active = true;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getArtisanId() { return artisanId; }
    public void setArtisanId(UUID artisanId) { this.artisanId = artisanId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCraftType() { return craftType; }
    public void setCraftType(String craftType) { this.craftType = craftType; }

    public String getOriginVillage() { return originVillage; }
    public void setOriginVillage(String originVillage) { this.originVillage = originVillage; }

    public boolean isGiCertified() { return giCertified; }
    public void setGiCertified(boolean giCertified) { this.giCertified = giCertified; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPriceInr() { return priceInr; }
    public void setPriceInr(BigDecimal priceInr) { this.priceInr = priceInr; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public boolean isFeaturedFestival() { return featuredFestival; }
    public void setFeaturedFestival(boolean featuredFestival) { this.featuredFestival = featuredFestival; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
