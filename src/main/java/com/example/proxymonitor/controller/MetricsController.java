package com.example.proxymonitor.controller;

import com.example.proxymonitor.model.CallStatus;
import com.example.proxymonitor.model.PagedLogs;
import com.example.proxymonitor.model.ServiceSummary;
import com.example.proxymonitor.repository.LogRepository;
import com.example.proxymonitor.proxy.MicroserviceProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private final LogRepository repository;
    private final Map<String, MicroserviceProxy<Object>> proxies;
    private final List<String> inventoryOperations = List.of("checkStock", "listProducts");
    private final List<String> orderOperations = List.of("createOrder", "getOrder", "cancelOrder");
    private final List<String> paymentOperations = List.of("processPayment", "refundPayment", "getPaymentStatus");

    public MetricsController(LogRepository repository,
                             @Qualifier("inventoryProxy") MicroserviceProxy<Object> inventory,
                             @Qualifier("ordersProxy") MicroserviceProxy<Object> orders,
                             @Qualifier("paymentsProxy") MicroserviceProxy<Object> payments) {
        this.repository = repository;
        this.proxies = Map.of("inventory", inventory, "orders", orders, "payments", payments);
    }

    @GetMapping("/summary") public List<ServiceSummary> summary() { return repository.summary(); }

    @GetMapping("/logs")
    public PagedLogs logs(@RequestParam(required = false) String service,
                          @RequestParam(required = false) CallStatus status,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                          @RequestParam(required = false) String requestId,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "25") int size) {
        if (service != null && !service.isBlank() && !proxies.containsKey(service.toLowerCase())) throw new IllegalArgumentException("Unknown service filter: " + service);
        return repository.search(service, status, from, to, requestId, page, size);
    }

    @PostMapping("/simulate-load")
    public Map<String, Object> simulateLoad() {
        int failures = 0;
        for (int i = 0; i < 50; i++) {
            int serviceIndex = ThreadLocalRandom.current().nextInt(3);
            String service = List.of("inventory", "orders", "payments").get(serviceIndex);
            String operation = switch (service) {
                case "inventory" -> pick(inventoryOperations);
                case "orders" -> pick(orderOperations);
                default -> pick(paymentOperations);
            };
            Object[] params = switch (operation) {
                case "checkStock" -> new Object[]{pick(List.of("keyboard", "mouse", "monitor"))};
                case "reserveStock" -> new Object[]{pick(List.of("keyboard", "mouse", "monitor")), 1};
                case "createOrder" -> new Object[]{"keyboard", 1};
                case "getOrder", "cancelOrder" -> new Object[]{"ord_demo"};
                case "processPayment" -> new Object[]{"ord_demo", 49.99};
                case "refundPayment", "getPaymentStatus" -> new Object[]{"pay_demo"};
                default -> new Object[0];
            };
            try { proxies.get(service).execute(operation, params); }
            catch (RuntimeException exception) { failures++; }
        }
        return Map.of("generatedCalls", 50, "errors", failures, "message", "Load simulation completed");
    }

    private String pick(List<String> values) { return values.get(ThreadLocalRandom.current().nextInt(values.size())); }
}
