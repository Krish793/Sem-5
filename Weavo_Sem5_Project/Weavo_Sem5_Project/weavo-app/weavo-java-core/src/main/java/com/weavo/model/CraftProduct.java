package com.weavo.model;

import com.weavo.exception.InvalidInputException;
import com.weavo.exception.OutOfStockException;

/**
 * Domain entity representing an authentic handcrafted Gujarat artisan item.
 * Encapsulates thread-safe stock reduction logic and domain validation.
 */
public class CraftProduct {
    private String id;
    private String sku;
    private String title;
    private String craftType;
    private String originVillage;
    private boolean giCertified;
    private double priceInr;
    private int stockQty;
    private boolean active;

    public CraftProduct(String id, String sku, String title, String craftType, 
                        String originVillage, boolean giCertified, double priceInr, int stockQty) 
                        throws InvalidInputException {
        if (priceInr < 50.0) {
            throw new InvalidInputException("priceInr", "Minimum artisan craft price must be ₹50.00");
        }
        if (stockQty < 0) {
            throw new InvalidInputException("stockQty", "Stock quantity cannot be negative");
        }
        this.id = id;
        this.sku = sku;
        this.title = title;
        this.craftType = craftType;
        this.originVillage = originVillage;
        this.giCertified = giCertified;
        this.priceInr = priceInr;
        this.stockQty = stockQty;
        this.active = true;
    }

    /**
     * Deducts stock safely. Throws custom OutOfStockException if inventory is insufficient.
     */
    public synchronized void decrementStock(int quantity) throws OutOfStockException {
        if (quantity <= 0) {
            return;
        }
        if (this.stockQty < quantity) {
            throw new OutOfStockException(this.id, quantity, this.stockQty);
        }
        this.stockQty -= quantity;
    }

    public synchronized void restock(int quantity) {
        if (quantity > 0) {
            this.stockQty += quantity;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getSku() { return sku; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCraftType() { return craftType; }
    public String getOriginVillage() { return originVillage; }
    public boolean isGiCertified() { return giCertified; }
    public double getPriceInr() { return priceInr; }
    public synchronized void setPriceInr(double priceInr) { this.priceInr = priceInr; }
    public synchronized int getStockQty() { return stockQty; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return String.format("%s - %s [₹%.2f, Stock: %d, GI: %s]", sku, title, priceInr, stockQty, giCertified ? "YES" : "NO");
    }
}
