package com.weavo.payment;

import com.weavo.exception.PaymentFailedException;
import com.weavo.model.Order;

import java.util.UUID;

/**
 * Card / NetBanking Implementation of PaymentGateway interface.
 * Demonstrates Interface implementation and Polymorphism.
 */
public class CardPaymentGateway implements PaymentGateway {
    @Override
    public String getGatewayName() {
        return "RUPAY_VISA_TOKENIZED_GATEWAY";
    }

    @Override
    public String processPayment(Order order, double amount, boolean simulateFailure) throws PaymentFailedException {
        if (simulateFailure) {
            throw new PaymentFailedException("Card payment authentication failed: 3D Secure timeout", "CARD_3DS_FAILED", amount);
        }
        String txnRef = "CARD-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setPaymentStatus("PAID_CARD");
        order.setTransactionRef(txnRef);
        return txnRef;
    }

    @Override
    public boolean verifyTransaction(String transactionRef) {
        return transactionRef != null && transactionRef.startsWith("CARD-TXN-");
    }
}
