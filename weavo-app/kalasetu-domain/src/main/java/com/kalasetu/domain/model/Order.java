package com.kalasetu.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private BigDecimal totalAmountInr;
    private BigDecimal platformCommissionInr;
    private BigDecimal shippingFeeInr;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionRef;
    private String orderStatus;
    private String shippingAddress;
    private String dpdpConsentVersion = "v1.0-2026";
    private OffsetDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public BigDecimal getTotalAmountInr() { return totalAmountInr; }
    public void setTotalAmountInr(BigDecimal totalAmountInr) { this.totalAmountInr = totalAmountInr; }

    public BigDecimal getPlatformCommissionInr() { return platformCommissionInr; }
    public void setPlatformCommissionInr(BigDecimal platformCommissionInr) { this.platformCommissionInr = platformCommissionInr; }

    public BigDecimal getShippingFeeInr() { return shippingFeeInr; }
    public void setShippingFeeInr(BigDecimal shippingFeeInr) { this.shippingFeeInr = shippingFeeInr; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getDpdpConsentVersion() { return dpdpConsentVersion; }
    public void setDpdpConsentVersion(String dpdpConsentVersion) { this.dpdpConsentVersion = dpdpConsentVersion; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
