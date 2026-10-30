package com.example.proxymonitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(body("BAD_REQUEST", exception.getMessage()));
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleServiceFailure(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body("SERVICE_ERROR", exception.getMessage()));
    }
    private Map<String, Object> body(String code, String message) { return Map.of("timestamp", Instant.now(), "code", code, "message", message == null ? "Unexpected error" : message); }
}
