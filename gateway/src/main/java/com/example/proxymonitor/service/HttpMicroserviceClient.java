package com.example.proxymonitor.service;

import com.example.proxymonitor.model.ServiceCallRequest;
import com.example.proxymonitor.proxy.MicroserviceProxy;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public final class HttpMicroserviceClient implements MicroserviceProxy<Object> {
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private final RestClient client;

    public HttpMicroserviceClient(RestClient.Builder builder, String baseUrl, String internalToken) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(150));
        this.client = builder.baseUrl(baseUrl).defaultHeader(INTERNAL_TOKEN_HEADER, internalToken)
                .requestFactory(requestFactory).build();
    }

    @Override
    public Object execute(String operation, Object... params) {
        List<Object> arguments = params == null ? List.of() : Arrays.asList(params.clone());
        return client.post()
                .uri("/internal/{operation}", operation)
                .body(new ServiceCallRequest(arguments))
                .retrieve()
                .body(Object.class);
    }
}
