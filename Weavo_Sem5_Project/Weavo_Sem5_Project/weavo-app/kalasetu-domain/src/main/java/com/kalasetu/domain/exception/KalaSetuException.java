package com.kalasetu.domain.exception;

/**
 * Base custom checked exception for all KalaSetu domain & business rule violations.
 */
public abstract class KalaSetuException extends Exception {
    private final String errorCode;
    private final int httpStatus;

    protected KalaSetuException(String errorCode, int httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected KalaSetuException(String errorCode, int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
