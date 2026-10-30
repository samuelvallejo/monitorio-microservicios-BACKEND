package com.example.proxymonitor.service;

import com.example.proxymonitor.proxy.MicroserviceProxy;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService implements MicroserviceProxy<Object> {
    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();

    public OrderService() {
        orders.put("ord_demo", Map.of("orderId", "ord_demo", "productId", "keyboard", "quantity", 1,
                "status", "CREATED", "createdAt", Instant.now().toString()));
    }

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "createOrder" -> create(params);
            case "getOrder" -> get(params);
            case "cancelOrder" -> cancel(params);
            default -> throw new IllegalArgumentException("Unknown orders operation: " + operation);
        };
    }

    private Map<String, Object> create(Object[] params) {
        String productId = params.length > 0 ? String.valueOf(params[0]) : "keyboard";
        int quantity = params.length > 1 ? Integer.parseInt(String.valueOf(params[1])) : 1;
        String id = "ord_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> order = Map.of("orderId", id, "productId", productId, "quantity", quantity,
                "status", "CREATED", "createdAt", Instant.now().toString());
        orders.put(id, order);
        return order;
    }

    private Map<String, Object> get(Object[] params) {
        Map<String, Object> order = orders.get(param(params));
        if (order == null) throw new IllegalArgumentException("Order was not found");
        return order;
    }

    private Map<String, Object> cancel(Object[] params) {
        String id = param(params);
        Map<String, Object> current = get(params);
        Map<String, Object> cancelled = Map.of("orderId", id, "productId", current.get("productId"),
                "quantity", current.get("quantity"), "status", "CANCELLED", "createdAt", current.get("createdAt"));
        orders.put(id, cancelled);
        return cancelled;
    }

    private String param(Object[] params) {
        if (params.length == 0) throw new IllegalArgumentException("An orderId parameter is required");
        return String.valueOf(params[0]);
    }
}
