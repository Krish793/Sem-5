package com.kalasetu.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentReceipt {
    private String transactionRef;
    private String methodCode;
    private String status;
    private BigDecimal amountInr;
    private boolean testMode;
    private String message;
    private OffsetDateTime processedAt;

    public PaymentReceipt(String transactionRef, String methodCode, String status,
                          BigDecimal amountInr, boolean testMode, String message) {
        this.transactionRef = transactionRef;
        this.methodCode = methodCode;
        this.status = status;
        this.amountInr = amountInr;
        this.testMode = testMode;
        this.message = message;
        this.processedAt = OffsetDateTime.now();
    }

    public String getTransactionRef() { return transactionRef; }
    public String getMethodCode() { return methodCode; }
    public String getStatus() { return status; }
    public BigDecimal getAmountInr() { return amountInr; }
    public boolean isTestMode() { return testMode; }
    public String getMessage() { return message; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
}
