package com.kalasetu.payment;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.PaymentFailedException;

import java.math.BigDecimal;

/**
 * PaymentMethod interface demonstrating polymorphism across simulated test-mode payment providers.
 */
public interface PaymentMethod {
    String getMethodCode();

    default boolean isTestModeOnly() {
        return true;
    }

    PaymentReceipt processPayment(PaymentRequest request) throws PaymentFailedException, InvalidInputException;

    PaymentReceipt refund(String transactionRef, BigDecimal amountInr) throws PaymentFailedException;
}
