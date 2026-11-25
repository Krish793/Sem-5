package com.weavo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a customer order and automated artisan payout settlement.
 */
public class Order {
    private String orderNumber;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmountInr;
    private double artisanPayoutInr;
    private double platformFeeInr;
    private String paymentStatus;
    private String transactionRef;

    public Order(String orderNumber, String customerId) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.totalAmountInr = 0.0;
        this.paymentStatus = "PENDING";
    }

    public void addItem(CraftProduct product, int quantity) {
        double lineTotal = product.getPriceInr() * quantity;
        this.items.add(new OrderItem(product.getId(), product.getTitle(), quantity, product.getPriceInr(), lineTotal));
        recalculateTotals();
    }

    private void recalculateTotals() {
        double subtotal = 0.0;
        for (OrderItem it : items) {
            subtotal += it.getLineTotal();
        }
        this.totalAmountInr = subtotal;
        this.artisanPayoutInr = Math.round(subtotal * 0.92 * 100.0) / 100.0; // 92% to village karigar
        this.platformFeeInr = Math.round((subtotal - this.artisanPayoutInr) * 100.0) / 100.0; // 8% platform fee
    }

    public String getOrderNumber() { return orderNumber; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> items() { return items; }
    public double getTotalAmountInr() { return totalAmountInr; }
    public double getArtisanPayoutInr() { return artisanPayoutInr; }
    public double getPlatformFeeInr() { return platformFeeInr; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public static class OrderItem {
        private String productId;
        private String title;
        private int quantity;
        private double unitPrice;
        private double lineTotal;

        public OrderItem(String productId, String title, int quantity, double unitPrice, double lineTotal) {
            this.productId = productId;
            this.title = title;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }

        public String getProductId() { return productId; }
        public String getTitle() { return title; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getLineTotal() { return lineTotal; }
    }
}
