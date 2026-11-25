package com.kalasetu.web.controller;

import com.kalasetu.domain.exception.KalaSetuException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KalaSetuException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(KalaSetuException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(Map.of(
                "errorCode", ex.getErrorCode(),
                "message", ex.getMessage(),
                "status", ex.getHttpStatus(),
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "errorCode", "BAD_REQUEST",
                "message", ex.getMessage(),
                "status", 400,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "errorCode", "INTERNAL_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected server error",
                "status", 500,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}
