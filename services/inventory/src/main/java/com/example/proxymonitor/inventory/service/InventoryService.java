package com.example.proxymonitor.inventory.service;

import com.example.proxymonitor.proxy.MicroserviceProxy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService implements MicroserviceProxy<Object> {
    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of("keyboard", 24, "mouse", 42, "monitor", 11));

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "listProducts" -> stock.entrySet().stream()
                    .map(entry -> Map.of("productId", entry.getKey(), "quantity", entry.getValue())).toList();
            case "checkStock" -> {
                String product = stringParam(params, 0, "keyboard");
                int quantity = stock.getOrDefault(product, 0);
                yield Map.of("productId", product, "quantity", quantity, "available", quantity > 0);
            }
            case "reserveStock" -> reserve(stringParam(params, 0, "keyboard"), intParam(params, 1, 1));
            default -> throw new IllegalArgumentException("Unknown inventory operation: " + operation);
        };
    }

    private Map<String, Object> reserve(String product, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive");
        int remaining = stock.compute(product, (key, current) -> {
            if (current == null || current < quantity) throw new IllegalArgumentException("Insufficient stock for " + product);
            return current - quantity;
        });
        return Map.of("productId", product, "reserved", quantity, "remaining", remaining);
    }

    private String stringParam(Object[] params, int index, String fallback) {
        return params.length > index ? String.valueOf(params[index]) : fallback;
    }

    private int intParam(Object[] params, int index, int fallback) {
        return params.length > index ? Integer.parseInt(String.valueOf(params[index])) : fallback;
    }
}
