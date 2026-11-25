package com.kalasetu.payment;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.PaymentFailedException;
import com.kalasetu.domain.security.InputSanitizer;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletTestPayment implements PaymentMethod {
    private static final BigDecimal SANDBOX_WALLET_CAP = new BigDecimal("25000");

    @Override
    public String getMethodCode() {
        return "WALLET_TEST";
    }

    @Override
    public PaymentReceipt processPayment(PaymentRequest request) throws PaymentFailedException, InvalidInputException {
        BigDecimal amount = InputSanitizer.requirePositiveAmount("amountInr", request.getAmountInr());
        if (request.isSimulateFailure() || amount.compareTo(SANDBOX_WALLET_CAP) > 0) {
            throw new PaymentFailedException(getMethodCode(), "Test wallet balance cap (₹25,000) exceeded or declined");
        }
        String txRef = "WALLET-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReceipt(txRef, getMethodCode(), "PAID_TEST", amount, true,
                "Test-mode Wallet debit approved");
    }

    @Override
    public PaymentReceipt refund(String transactionRef, BigDecimal amountInr) throws PaymentFailedException {
        if (transactionRef == null || !transactionRef.startsWith("WALLET-TEST-")) {
            throw new PaymentFailedException(getMethodCode(), "Invalid test wallet transaction reference");
        }
        return new PaymentReceipt("REF-" + transactionRef, getMethodCode(), "REFUNDED_TEST",
                amountInr, true, "Test-mode Wallet balance restored");
    }
}
