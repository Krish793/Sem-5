package com.weavo.exception;

/**
 * Base custom exception for the Weavo Artisan Marketplace platform.
 * Demonstrates Custom Exception Hierarchy in Java.
 */
public class WeavoException extends Exception {
    private final String errorCode;

    public WeavoException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public WeavoException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
