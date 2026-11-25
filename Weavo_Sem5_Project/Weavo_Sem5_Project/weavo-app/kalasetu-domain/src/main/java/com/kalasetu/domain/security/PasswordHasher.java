package com.kalasetu.domain.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Salted SHA-256 password hashing utility with constant-time verification.
 */
public final class PasswordHasher {
    private static final String SALT_PREFIX = "weavo-v1:";

    private PasswordHasher() {}

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest((SALT_PREFIX + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(encoded.length * 2);
            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static boolean matches(String rawPassword, String expectedHash) {
        if (rawPassword == null || expectedHash == null) {
            return false;
        }
        byte[] computed = hash(rawPassword).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(computed, expected);
    }
}
