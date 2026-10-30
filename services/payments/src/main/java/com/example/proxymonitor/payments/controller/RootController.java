package com.example.proxymonitor.payments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {
    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of("service", "payments-service", "status", "UP", "message", "Use the API gateway for monitored requests.", "health", "/actuator/health");
    }
}
