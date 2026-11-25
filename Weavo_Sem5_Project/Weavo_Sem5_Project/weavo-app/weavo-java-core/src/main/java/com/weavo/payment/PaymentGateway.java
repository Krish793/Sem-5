package com.weavo.payment;

import com.weavo.exception.PaymentFailedException;
import com.weavo.model.Order;

/**
 * Interface demonstrating Polymorphism and Interface Abstraction in Java.
 * Multiple simulated payment implementations (UPI, Card, NetBanking) implement this contract.
 */
public interface PaymentGateway {
    String getGatewayName();

    /**
     * Executes payment processing. Throws custom PaymentFailedException if declined.
     */
    String processPayment(Order order, double amount, boolean simulateFailure) throws PaymentFailedException;

    boolean verifyTransaction(String transactionRef);
}
