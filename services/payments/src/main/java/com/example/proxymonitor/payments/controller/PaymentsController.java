package com.example.proxymonitor.payments.controller;

import com.example.proxymonitor.payments.model.ServiceCallRequest;
import com.example.proxymonitor.payments.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/internal")
public class PaymentsController {
    private final PaymentService service;
    private final byte[] expectedToken;

    public PaymentsController(PaymentService service, @Value("${app.internal-token}") String token) {
        this.service = service;
        this.expectedToken = token.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/{operation}")
    public Object execute(@PathVariable String operation, @RequestBody(required = false) ServiceCallRequest request,
                          @RequestHeader(name = "X-Internal-Token", required = false) String token) {
        if (token == null || !MessageDigest.isEqual(expectedToken, token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal service token");
        }
        List<Object> params = request == null || request.params() == null ? List.of() : request.params();
        return service.execute(operation, params.toArray());
    }
}
