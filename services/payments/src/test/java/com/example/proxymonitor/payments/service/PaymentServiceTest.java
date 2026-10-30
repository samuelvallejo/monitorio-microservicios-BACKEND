package com.example.proxymonitor.payments.service;

import com.example.proxymonitor.payments.exception.PaymentFailedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {
    @Test
    void deterministicRandomSourceCanForcePaymentFailure() {
        PaymentService service = new PaymentService(() -> 0.09);
        assertThrows(PaymentFailedException.class, () -> service.execute("processPayment", "ord_demo", 20));
    }

    @Test
    void valueOutsideFailureWindowProcessesPayment() {
        PaymentService service = new PaymentService(() -> 0.10);
        assertTrue(service.execute("processPayment", "ord_demo", 20).toString().contains("AUTHORIZED"));
    }
}
