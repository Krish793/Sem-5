package com.kalasetu.payment;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.PaymentFailedException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Orchestrates test-mode payment methods. Explicitly rejects live production charges per academic guidelines.
 */
public class TestPaymentGatewayService {
    private final Map<String, PaymentMethod> registry = Map.of(
        "UPI_TEST", new UpiTestPayment(),
        "CARD_TEST", new CardTestPayment(),
        "WALLET_TEST", new WalletTestPayment()
    );

    public PaymentReceipt chargeTestMode(PaymentRequest request) throws PaymentFailedException, InvalidInputException {
        if (request == null || request.getPaymentMethodCode() == null) {
            throw new InvalidInputException("paymentMethod", "must be provided (UPI_TEST, CARD_TEST, WALLET_TEST)");
        }
        String code = request.getPaymentMethodCode().trim().toUpperCase();
        PaymentMethod method = registry.get(code);
        if (method == null) {
            throw new InvalidInputException("paymentMethod", "Only test-mode methods allowed: UPI_TEST, CARD_TEST, WALLET_TEST");
        }
        if (!method.isTestModeOnly()) {
            throw new PaymentFailedException(code, "Real payment gateways are disabled in Weavo Sem 5 build");
        }
        return method.processPayment(request);
    }

    public PaymentReceipt refundTestMode(String methodCode, String transactionRef, BigDecimal amountInr)
            throws PaymentFailedException, InvalidInputException {
        PaymentMethod method = registry.get(methodCode == null ? "" : methodCode.trim().toUpperCase());
        if (method == null) {
            throw new InvalidInputException("paymentMethod", "Unknown test payment method");
        }
        return method.refund(transactionRef, amountInr);
    }
}
