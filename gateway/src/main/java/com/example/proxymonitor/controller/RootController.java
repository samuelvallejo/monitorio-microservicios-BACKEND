package com.example.proxymonitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {
    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "service", "microservice-audit-monitor-api",
                "status", "UP",
                "dashboard", "https://monitorio-microservicios-frontend.vercel.app",
                "health", "/actuator/health",
                "endpoints", Map.of(
                        "summary", "GET /api/metrics/summary",
                        "logs", "GET /api/metrics/logs",
                        "simulateLoad", "POST /api/metrics/simulate-load",
                        "inventory", "POST /api/services/inventory/{operation}",
                        "orders", "POST /api/services/orders/{operation}",
                        "payments", "POST /api/services/payments/{operation}"
                )
        );
    }
}
