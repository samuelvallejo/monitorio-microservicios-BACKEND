package com.example.proxymonitor.payments.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) { super(message); }
}
