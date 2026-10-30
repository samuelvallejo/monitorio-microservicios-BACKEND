package com.example.proxymonitor.controller;

import com.example.proxymonitor.model.ServiceCallRequest;
import com.example.proxymonitor.proxy.MicroserviceProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final Map<String, MicroserviceProxy<Object>> proxies;
    public ServiceController(@Qualifier("inventoryProxy") MicroserviceProxy<Object> inventory,
                             @Qualifier("ordersProxy") MicroserviceProxy<Object> orders,
                             @Qualifier("paymentsProxy") MicroserviceProxy<Object> payments) {
        this.proxies = Map.of("inventory", inventory, "orders", orders, "payments", payments);
    }

    @PostMapping("/{service}/{operation}")
    public ResponseEntity<Object> execute(@PathVariable String service, @PathVariable String operation,
                                          @RequestBody(required = false) ServiceCallRequest request) {
        MicroserviceProxy<Object> proxy = proxies.get(service.toLowerCase());
        if (proxy == null) throw new IllegalArgumentException("Unknown service: " + service);
        Object[] params = request == null || request.params() == null ? new Object[0] : request.params().toArray();
        return ResponseEntity.ok(proxy.execute(operation, params));
    }
}
