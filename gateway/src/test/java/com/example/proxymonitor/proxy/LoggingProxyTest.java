package com.example.proxymonitor.proxy;

import com.example.proxymonitor.model.CallStatus;
import com.example.proxymonitor.repository.LogRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoggingProxyTest {
    @Test
    void recordsSuccessfulCallWithUniqueRequestIdAndDuration() {
        LogRepository repository = new LogRepository();
        MicroserviceProxy<String> realService = (operation, params) -> "ok";
        LoggingProxy<String> proxy = new LoggingProxy<>("inventory", realService, repository);

        assertEquals("ok", proxy.execute("listProducts"));
        proxy.execute("listProducts");

        var logs = repository.recent();
        assertEquals(2, logs.size());
        assertEquals(CallStatus.SUCCESS, logs.get(0).status());
        assertTrue(logs.get(0).durationMs() >= 0);
        assertDoesNotThrow(() -> UUID.fromString(logs.get(0).requestId()));
        assertNotEquals(logs.get(0).requestId(), logs.get(1).requestId());
    }

    @Test
    void recordsServiceFailureAndRethrowsSameException() {
        LogRepository repository = new LogRepository();
        IllegalStateException failure = new IllegalStateException("service unavailable");
        LoggingProxy<Object> proxy = new LoggingProxy<>("payments", (operation, params) -> { throw failure; }, repository);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> proxy.execute("processPayment"));

        assertSame(failure, thrown);
        var log = repository.recent().get(0);
        assertEquals(CallStatus.ERROR, log.status());
        assertEquals("service unavailable", log.errorMessage());
        assertFalse(log.stackTraceSummary().isBlank());
    }
}
