package com.kalasetu.domain.exception;

/** Thrown when a user attempts a role-restricted operation without proper authorization. */
public class UnauthorizedActionException extends KalaSetuException {
    public UnauthorizedActionException(String message) {
        super("UNAUTHORIZED_ACTION", 403, message);
    }
}
