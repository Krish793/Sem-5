package com.weavo.payment;

import com.weavo.exception.PaymentFailedException;
import com.weavo.model.Order;

import java.util.UUID;

/**
 * UPI Implementation of PaymentGateway interface.
 * Demonstrates Interface implementation and Polymorphism.
 */
public class UpiPaymentGateway implements PaymentGateway {
    @Override
    public String getGatewayName() {
        return "NPCI_BHIM_UPI_SANDBOX";
    }

    @Override
    public String processPayment(Order order, double amount, boolean simulateFailure) throws PaymentFailedException {
        if (simulateFailure) {
            throw new PaymentFailedException("Simulated UPI payment declined by user bank", "UPI_DECLINE_SIMULATED", amount);
        }
        String txnRef = "UPI-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setPaymentStatus("PAID_UPI");
        order.setTransactionRef(txnRef);
        return txnRef;
    }

    @Override
    public boolean verifyTransaction(String transactionRef) {
        return transactionRef != null && transactionRef.startsWith("UPI-TXN-");
    }
}
