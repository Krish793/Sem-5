package com.weavo.exception;

/**
 * Custom Exception thrown when payment verification or gateway settlement fails.
 */
public class PaymentFailedException extends WeavoException {
    private final String transactionRef;
    private final double amount;

    public PaymentFailedException(String message, String transactionRef, double amount) {
        super(message, "ERR_PAYMENT_FAILED");
        this.transactionRef = transactionRef;
        this.amount = amount;
    }

    public String getTransactionRef() { return transactionRef; }
    public double getAmount() { return amount; }
}
