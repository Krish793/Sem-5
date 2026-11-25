package com.kalasetu.domain.exception;

/** Thrown when a test-mode payment transaction fails validation or simulated bank authorization. */
public class PaymentFailedException extends KalaSetuException {
    private final String paymentMethod;
    private final String failureReason;

    public PaymentFailedException(String paymentMethod, String failureReason) {
        super(
            "PAYMENT_FAILED",
            402,
            String.format("Test payment via %s failed: %s", paymentMethod, failureReason)
        );
        this.paymentMethod = paymentMethod;
        this.failureReason = failureReason;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public String getFailureReason() { return failureReason; }
}
