package com.example.proxymonitor.repository;

import com.example.proxymonitor.model.CallStatus;
import com.example.proxymonitor.model.LogEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogRepositoryTest {
    @Test
    void filtersAndPaginatesEntries() {
        LogRepository repository = new LogRepository();
        Instant now = Instant.now();
        repository.save(entry("inventory", CallStatus.SUCCESS, now, "a"));
        repository.save(entry("payments", CallStatus.ERROR, now, "b"));
        repository.save(entry("inventory", CallStatus.ERROR, now.minusSeconds(60), "c"));

        var page = repository.search("inventory", CallStatus.SUCCESS, now.minusSeconds(5), now.plusSeconds(5), null, 0, 1);

        assertEquals(1, page.totalElements());
        assertEquals(1, page.content().size());
        assertEquals("a", page.content().get(0).requestId());
        assertEquals(1, page.totalPages());
    }

    @Test
    void summarizesCallsErrorsAndAverageDuration() {
        LogRepository repository = new LogRepository();
        repository.save(new LogEntry("inventory", "checkStock", 10, CallStatus.SUCCESS, "1", Instant.now(), List.of(), Map.of(), null, null));
        repository.save(new LogEntry("inventory", "checkStock", 30, CallStatus.ERROR, "2", Instant.now(), List.of(), null, "failed", "summary"));

        var summary = repository.summary().stream().filter(row -> row.serviceId().equals("inventory")).findFirst().orElseThrow();

        assertEquals(2, summary.totalCalls());
        assertEquals(50.0, summary.errorRate());
        assertEquals(50.0, summary.successRate());
        assertEquals(20.0, summary.averageDurationMs());
    }

    private LogEntry entry(String service, CallStatus status, Instant timestamp, String id) {
        return new LogEntry(service, "operation", 5, status, id, timestamp, List.of(), null, null, null);
    }
}
