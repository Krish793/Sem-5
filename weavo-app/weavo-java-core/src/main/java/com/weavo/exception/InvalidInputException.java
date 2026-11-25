package com.weavo.exception;

/**
 * Custom Exception thrown when product attributes, price, or user input fail validation.
 */
public class InvalidInputException extends WeavoException {
    private final String fieldName;

    public InvalidInputException(String fieldName, String message) {
        super(String.format("Invalid value for field '%s': %s", fieldName, message), "ERR_INVALID_INPUT");
        this.fieldName = fieldName;
    }

    public String getFieldName() { return fieldName; }
}
