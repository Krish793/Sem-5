package com.kalasetu.payment;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.PaymentFailedException;
import com.kalasetu.domain.security.InputSanitizer;

import java.math.BigDecimal;
import java.util.UUID;

public class UpiTestPayment implements PaymentMethod {
    private static final BigDecimal UPI_MAX_LIMIT = new BigDecimal("100000");

    @Override
    public String getMethodCode() {
        return "UPI_TEST";
    }

    @Override
    public PaymentReceipt processPayment(PaymentRequest request) throws PaymentFailedException, InvalidInputException {
        BigDecimal amount = InputSanitizer.requirePositiveAmount("amountInr", request.getAmountInr());
        String vpa = request.getPaymentToken() != null ? request.getPaymentToken().trim() : "demo@okkalasetu";

        if (!vpa.contains("@")) {
            throw new InvalidInputException("paymentToken", "UPI ID must contain '@' (e.g., buyer@okaxis)");
        }
        if (request.isSimulateFailure() || vpa.toLowerCase().startsWith("fail@")) {
            throw new PaymentFailedException(getMethodCode(), "Simulated NPCI sandbox decline for VPA " + vpa);
        }
        if (amount.compareTo(UPI_MAX_LIMIT) > 0) {
            throw new PaymentFailedException(getMethodCode(), "Exceeds ₹1,00,000 per-transaction UPI sandbox limit");
        }
        String txRef = "UPI-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReceipt(txRef, getMethodCode(), "PAID_TEST", amount, true,
                "Test-mode UPI payment approved (no real funds transferred)");
    }

    @Override
    public PaymentReceipt refund(String transactionRef, BigDecimal amountInr) throws PaymentFailedException {
        if (transactionRef == null || !transactionRef.startsWith("UPI-TEST-")) {
            throw new PaymentFailedException(getMethodCode(), "Invalid test transaction reference for refund");
        }
        return new PaymentReceipt("REF-" + transactionRef, getMethodCode(), "REFUNDED_TEST",
                amountInr, true, "Test-mode UPI refund queued");
    }
}
