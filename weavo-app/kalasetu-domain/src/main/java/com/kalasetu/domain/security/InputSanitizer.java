package com.kalasetu.domain.security;

import com.kalasetu.domain.exception.InvalidInputException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Input validation & security sanitization against XSS, SQL injection payloads, and malformed data.
 */
public final class InputSanitizer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern XSS_PATTERN = Pattern.compile("(?i)<\\s*script.*?>|<\\s*/\\s*script\\s*>|on\\w+\\s*=|javascript:");
    private static final Pattern SQLI_PATTERN = Pattern.compile("(?i)('\\s*OR\\s+'?1'?\\s*=\\s*'?1|;\\s*DROP\\s+TABLE|UNION\\s+SELECT|--\\s*$)");

    private InputSanitizer() {}

    public static String sanitizeText(String field, String value, int maxLen) throws InvalidInputException {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(field, "must not be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLen) {
            throw new InvalidInputException(field, "exceeds maximum length of " + maxLen);
        }
        if (XSS_PATTERN.matcher(trimmed).find()) {
            throw new InvalidInputException(field, "contains potentially unsafe HTML/script content");
        }
        if (SQLI_PATTERN.matcher(trimmed).find()) {
            throw new InvalidInputException(field, "contains disallowed SQL pattern");
        }
        return trimmed.replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String validateEmail(String email) throws InvalidInputException {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("email", "must be a valid email address");
        }
        return email.trim().toLowerCase();
    }

    public static int requirePositiveInt(String field, int value, int max) throws InvalidInputException {
        if (value <= 0 || value > max) {
            throw new InvalidInputException(field, "must be between 1 and " + max);
        }
        return value;
    }

    public static BigDecimal requirePositiveAmount(String field, BigDecimal amount) throws InvalidInputException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException(field, "must be greater than zero");
        }
        return amount;
    }
}
