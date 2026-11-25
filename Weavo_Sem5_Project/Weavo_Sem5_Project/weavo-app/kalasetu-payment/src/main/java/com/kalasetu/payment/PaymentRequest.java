package com.kalasetu.payment;

import java.math.BigDecimal;

public class PaymentRequest {
    private String orderNumber;
    private BigDecimal amountInr;
    private String paymentMethodCode;
    private String paymentToken; // e.g., test UPI ID like buyer@okaxis, test card last4, or wallet ref
    private boolean simulateFailure;

    public PaymentRequest() {}

    public PaymentRequest(String orderNumber, BigDecimal amountInr, String paymentMethodCode,
                          String paymentToken, boolean simulateFailure) {
        this.orderNumber = orderNumber;
        this.amountInr = amountInr;
        this.paymentMethodCode = paymentMethodCode;
        this.paymentToken = paymentToken;
        this.simulateFailure = simulateFailure;
    }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public BigDecimal getAmountInr() { return amountInr; }
    public void setAmountInr(BigDecimal amountInr) { this.amountInr = amountInr; }

    public String getPaymentMethodCode() { return paymentMethodCode; }
    public void setPaymentMethodCode(String paymentMethodCode) { this.paymentMethodCode = paymentMethodCode; }

    public String getPaymentToken() { return paymentToken; }
    public void setPaymentToken(String paymentToken) { this.paymentToken = paymentToken; }

    public boolean isSimulateFailure() { return simulateFailure; }
    public void setSimulateFailure(boolean simulateFailure) { this.simulateFailure = simulateFailure; }
}
