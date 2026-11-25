package com.kalasetu.payment;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.PaymentFailedException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentModuleTest {
    private final TestPaymentGatewayService gateway = new TestPaymentGatewayService();

    @Test
    void testUpiTestPaymentSuccessAndFailurePaths() throws Exception {
        PaymentReceipt ok = gateway.chargeTestMode(new PaymentRequest(
                "KS-1", new BigDecimal("1500.00"), "UPI_TEST", "aarav@okicici", false));
        assertTrue(ok.isTestMode());
        assertEquals("PAID_TEST", ok.getStatus());
        assertTrue(ok.getTransactionRef().startsWith("UPI-TEST-"));

        PaymentReceipt refund = gateway.refundTestMode("UPI_TEST", ok.getTransactionRef(), new BigDecimal("1500.00"));
        assertEquals("REFUNDED_TEST", refund.getStatus());

        assertThrows(PaymentFailedException.class, () -> gateway.chargeTestMode(
                new PaymentRequest("KS-2", new BigDecimal("1500"), "UPI_TEST", "fail@upi", false)));
        assertThrows(InvalidInputException.class, () -> gateway.chargeTestMode(
                new PaymentRequest("KS-3", new BigDecimal("1500"), "UPI_TEST", "invalid-vpa", false)));
    }

    @Test
    void testCardAndWalletTestPayments() throws Exception {
        PaymentReceipt cardOk = gateway.chargeTestMode(new PaymentRequest(
                "KS-4", new BigDecimal("4850"), "CARD_TEST", "4242", false));
        assertEquals("PAID_TEST", cardOk.getStatus());

        assertThrows(PaymentFailedException.class, () -> gateway.chargeTestMode(
                new PaymentRequest("KS-5", new BigDecimal("4850"), "CARD_TEST", "0000", false)));

        PaymentReceipt walletOk = gateway.chargeTestMode(new PaymentRequest(
                "KS-6", new BigDecimal("950"), "WALLET_TEST", "wallet-demo", false));
        assertEquals("PAID_TEST", walletOk.getStatus());

        assertThrows(PaymentFailedException.class, () -> gateway.chargeTestMode(
                new PaymentRequest("KS-7", new BigDecimal("30000"), "WALLET_TEST", "wallet-demo", false)));

        assertThrows(InvalidInputException.class, () -> gateway.chargeTestMode(
                new PaymentRequest("KS-8", new BigDecimal("500"), "REAL_RAZORPAY", "tok", false)));
    }
}
