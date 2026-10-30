package com.example.proxymonitor.config;

import com.example.proxymonitor.proxy.LoggingProxy;
import com.example.proxymonitor.proxy.MicroserviceProxy;
import com.example.proxymonitor.repository.LogRepository;
import com.example.proxymonitor.service.InventoryService;
import com.example.proxymonitor.service.OrderService;
import com.example.proxymonitor.service.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {
    @Bean("inventoryProxy") MicroserviceProxy<Object> inventoryProxy(LogRepository logs) { return new LoggingProxy<>("inventory", new InventoryService(), logs); }
    @Bean("ordersProxy") MicroserviceProxy<Object> ordersProxy(LogRepository logs) { return new LoggingProxy<>("orders", new OrderService(), logs); }
    @Bean("paymentsProxy") MicroserviceProxy<Object> paymentsProxy(LogRepository logs) { return new LoggingProxy<>("payments", new PaymentService(), logs); }
}
