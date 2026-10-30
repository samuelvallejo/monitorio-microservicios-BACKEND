package com.example.proxymonitor.service;

import com.example.proxymonitor.exception.PaymentFailedException;
import com.example.proxymonitor.proxy.MicroserviceProxy;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;

public class PaymentService implements MicroserviceProxy<Object> {
    private final DoubleSupplier random;
    private final Map<String, Map<String, Object>> payments = new ConcurrentHashMap<>();

    public PaymentService() { this(Math::random); }
    public PaymentService(DoubleSupplier random) {
        this.random = random;
        payments.put("pay_demo", Map.of("paymentId", "pay_demo", "orderId", "ord_demo", "amount", 49.99,
                "status", "AUTHORIZED", "createdAt", Instant.now().toString()));
    }

    @Override
    public Object execute(String operation, Object... params) {
        if (random.getAsDouble() < 0.10) throw new PaymentFailedException("Payment provider declined the request");
        return switch (operation) {
            case "processPayment" -> process(params);
            case "refundPayment" -> refund(params);
            case "getPaymentStatus" -> get(params);
            default -> throw new IllegalArgumentException("Unknown payments operation: " + operation);
        };
    }

    private Map<String, Object> process(Object[] params) {
        String orderId = params.length > 0 ? String.valueOf(params[0]) : "ord_demo";
        double amount = params.length > 1 ? Double.parseDouble(String.valueOf(params[1])) : 49.99;
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        String id = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> payment = Map.of("paymentId", id, "orderId", orderId, "amount", amount,
                "status", "AUTHORIZED", "createdAt", Instant.now().toString());
        payments.put(id, payment);
        return payment;
    }

    private Map<String, Object> refund(Object[] params) {
        Map<String, Object> current = get(params);
        String id = String.valueOf(current.get("paymentId"));
        Map<String, Object> refunded = Map.of("paymentId", id, "orderId", current.get("orderId"),
                "amount", current.get("amount"), "status", "REFUNDED", "createdAt", current.get("createdAt"));
        payments.put(id, refunded);
        return refunded;
    }

    private Map<String, Object> get(Object[] params) {
        if (params.length == 0) throw new IllegalArgumentException("A paymentId parameter is required");
        Map<String, Object> payment = payments.get(String.valueOf(params[0]));
        if (payment == null) throw new IllegalArgumentException("Payment was not found");
        return payment;
    }
}
