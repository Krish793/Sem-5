package com.kalasetu.domain;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.OutOfStockException;
import com.kalasetu.domain.exception.PaymentFailedException;
import com.kalasetu.domain.exception.UnauthorizedActionException;
import com.kalasetu.domain.model.*;
import com.kalasetu.domain.security.InputSanitizer;
import com.kalasetu.domain.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainModuleTest {

    @Test
    void testUserInheritanceAndPolymorphism() {
        User customer = new Customer(UUID.randomUUID(), "Aarav Patel", "aarav@test.in", "hash", "+91", "Ahmedabad", "Gujarat", true);
        User giArtisan = new Artisan(UUID.randomUUID(), "Rameshbhai", "ramesh@test.in", "hash", "+91", "Jamnagar", "Gujarat", true,
                "Khatri Kala", "Jamnagar", "GI-GJ-BANDHANI-2019", "VERIFIED", "STARTER_FREE");
        User pendingArtisan = new Artisan(UUID.randomUUID(), "New Karigar", "new@test.in", "hash", "+91", "Kutch", "Gujarat", true,
                "Clay Craft", "Bhuj", null, "PENDING", "STARTER_FREE");
        User admin = new Admin(UUID.randomUUID(), "Admin", "admin@test.in", "hash", "+91", "Ahmedabad", "Gujarat", true);

        assertEquals(Role.CUSTOMER, customer.getRole());
        assertEquals(0.0, customer.calculatePlatformFeeRate());
        assertFalse(customer.canListCraft());

        assertEquals(Role.ARTISAN, giArtisan.getRole());
        assertEquals(0.08, giArtisan.calculatePlatformFeeRate());
        assertTrue(giArtisan.canListCraft());

        assertEquals(0.10, pendingArtisan.calculatePlatformFeeRate());
        assertFalse(pendingArtisan.canListCraft());

        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.canListCraft());
        assertTrue(admin.getDashboardCapabilities().contains("APPROVE_ARTISAN_KYC"));
    }

    @Test
    void testPasswordHasherMatchesSeededHash() {
        String hash = PasswordHasher.hash("Weavo@2026");
        assertEquals("86e794475ee6d91f4ee05430e3fe2c331b9860b381dd1a6a597f87b27b47fb11", hash);
        assertTrue(PasswordHasher.matches("Weavo@2026", hash));
        assertFalse(PasswordHasher.matches("WrongPass", hash));
        assertFalse(PasswordHasher.matches(null, hash));
    }

    @Test
    void testInputSanitizerBlocksXssAndSqli() throws InvalidInputException {
        assertEquals("Valid Bandhani Saree", InputSanitizer.sanitizeText("title", "  Valid Bandhani Saree  ", 100));
        assertThrows(InvalidInputException.class, () -> InputSanitizer.sanitizeText("comment", "<script>alert(1)</script>", 200));
        assertThrows(InvalidInputException.class, () -> InputSanitizer.sanitizeText("search", "' OR '1'='1", 100));
        assertThrows(InvalidInputException.class, () -> InputSanitizer.sanitizeText("search", "test; DROP TABLE products", 100));
        assertThrows(InvalidInputException.class, () -> InputSanitizer.validateEmail("bad-email"));
        assertEquals("buyer@weavo.in", InputSanitizer.validateEmail("Buyer@Weavo.in"));
        assertThrows(InvalidInputException.class, () -> InputSanitizer.requirePositiveInt("qty", 0, 10));
        assertThrows(InvalidInputException.class, () -> InputSanitizer.requirePositiveAmount("price", BigDecimal.ZERO));
    }

    @Test
    void testCustomExceptionProperties() {
        OutOfStockException oos = new OutOfStockException("Patola Dupatta", 2, 5);
        assertEquals("OUT_OF_STOCK", oos.getErrorCode());
        assertEquals(409, oos.getHttpStatus());
        assertEquals(2, oos.getAvailableQty());

        PaymentFailedException pfe = new PaymentFailedException("UPI_TEST", "Declined");
        assertEquals(402, pfe.getHttpStatus());
        assertEquals("UPI_TEST", pfe.getPaymentMethod());

        UnauthorizedActionException uae = new UnauthorizedActionException("Admin only");
        assertEquals(403, uae.getHttpStatus());
    }
}
