package com.example.proxymonitor.service;

import com.example.proxymonitor.exception.PaymentFailedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {
    @Test
    void deterministicRandomSourceCanForcePaymentFailure() {
        PaymentService service = new PaymentService(() -> 0.09);
        assertThrows(PaymentFailedException.class, () -> service.execute("processPayment", "ord_demo", 20));
    }

    @Test
    void randomValueOutsideFailureWindowProcessesPayment() {
        PaymentService service = new PaymentService(() -> 0.10);
        Object result = service.execute("processPayment", "ord_demo", 20);
        assertTrue(result.toString().contains("AUTHORIZED"));
    }
}
