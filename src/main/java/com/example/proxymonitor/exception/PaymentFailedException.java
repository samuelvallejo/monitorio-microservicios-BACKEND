package com.example.proxymonitor.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) { super(message); }
}
