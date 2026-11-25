package com.kalasetu.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private UUID id;
    private UUID orderId;
    private UUID productId;
    private UUID artisanId;
    private String productTitle;
    private int quantity;
    private BigDecimal unitPriceInr;
    private BigDecimal lineTotalInr;
    private BigDecimal artisanPayoutInr;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getArtisanId() { return artisanId; }
    public void setArtisanId(UUID artisanId) { this.artisanId = artisanId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPriceInr() { return unitPriceInr; }
    public void setUnitPriceInr(BigDecimal unitPriceInr) { this.unitPriceInr = unitPriceInr; }

    public BigDecimal getLineTotalInr() { return lineTotalInr; }
    public void setLineTotalInr(BigDecimal lineTotalInr) { this.lineTotalInr = lineTotalInr; }

    public BigDecimal getArtisanPayoutInr() { return artisanPayoutInr; }
    public void setArtisanPayoutInr(BigDecimal artisanPayoutInr) { this.artisanPayoutInr = artisanPayoutInr; }
}
