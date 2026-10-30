package com.example.proxymonitor.config;

import com.example.proxymonitor.proxy.LoggingProxy;
import com.example.proxymonitor.proxy.MicroserviceProxy;
import com.example.proxymonitor.repository.LogRepository;
import com.example.proxymonitor.service.HttpMicroserviceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProxyConfig {
    @Bean("inventoryProxy")
    MicroserviceProxy<Object> inventoryProxy(LogRepository logs,
            @Value("${services.inventory.url}") String url, @Value("${app.internal-token}") String token) {
        return new LoggingProxy<>("inventory", new HttpMicroserviceClient(RestClient.builder(), url, token), logs);
    }

    @Bean("ordersProxy")
    MicroserviceProxy<Object> ordersProxy(LogRepository logs,
            @Value("${services.orders.url}") String url, @Value("${app.internal-token}") String token) {
        return new LoggingProxy<>("orders", new HttpMicroserviceClient(RestClient.builder(), url, token), logs);
    }

    @Bean("paymentsProxy")
    MicroserviceProxy<Object> paymentsProxy(LogRepository logs,
            @Value("${services.payments.url}") String url, @Value("${app.internal-token}") String token) {
        return new LoggingProxy<>("payments", new HttpMicroserviceClient(RestClient.builder(), url, token), logs);
    }
}
