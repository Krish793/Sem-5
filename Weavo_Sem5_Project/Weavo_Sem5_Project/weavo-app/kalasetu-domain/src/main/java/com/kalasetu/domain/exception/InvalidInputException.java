package com.kalasetu.domain.exception;

/** Thrown when user input violates format, range, DPDP consent, or security sanitization checks. */
public class InvalidInputException extends KalaSetuException {
    private final String field;

    public InvalidInputException(String field, String message) {
        super("INVALID_INPUT", 400, String.format("Invalid '%s': %s", field, message));
        this.field = field;
    }

    public InvalidInputException(String field, String message, Throwable cause) {
        super("INVALID_INPUT", 400, String.format("Invalid '%s': %s", field, message), cause);
        this.field = field;
    }

    public String getField() { return field; }
}
