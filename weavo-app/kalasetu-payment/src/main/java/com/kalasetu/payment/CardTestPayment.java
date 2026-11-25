package com.kalasetu.payment;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.PaymentFailedException;
import com.kalasetu.domain.security.InputSanitizer;

import java.math.BigDecimal;
import java.util.UUID;

public class CardTestPayment implements PaymentMethod {
    @Override
    public String getMethodCode() {
        return "CARD_TEST";
    }

    @Override
    public PaymentReceipt processPayment(PaymentRequest request) throws PaymentFailedException, InvalidInputException {
        BigDecimal amount = InputSanitizer.requirePositiveAmount("amountInr", request.getAmountInr());
        String last4 = request.getPaymentToken() != null ? request.getPaymentToken().trim() : "4242";
        if (!last4.matches("\\d{4}")) {
            throw new InvalidInputException("paymentToken", "Test card token must be 4 digits (e.g., 4242)");
        }
        if (request.isSimulateFailure() || "0000".equals(last4)) {
            throw new PaymentFailedException(getMethodCode(), "Simulated 3DS sandbox failure for card ending " + last4);
        }
        String txRef = "CARD-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReceipt(txRef, getMethodCode(), "PAID_TEST", amount, true,
                "Test-mode Card authorization approved for **** " + last4);
    }

    @Override
    public PaymentReceipt refund(String transactionRef, BigDecimal amountInr) throws PaymentFailedException {
        if (transactionRef == null || !transactionRef.startsWith("CARD-TEST-")) {
            throw new PaymentFailedException(getMethodCode(), "Invalid test card transaction reference");
        }
        return new PaymentReceipt("REF-" + transactionRef, getMethodCode(), "REFUNDED_TEST",
                amountInr, true, "Test-mode Card refund simulated");
    }
}
