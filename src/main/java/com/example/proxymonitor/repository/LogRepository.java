package com.example.proxymonitor.repository;

import com.example.proxymonitor.model.CallStatus;
import com.example.proxymonitor.model.LogEntry;
import com.example.proxymonitor.model.PagedLogs;
import com.example.proxymonitor.model.ServiceSummary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class LogRepository {
    private static final int MAX_ENTRIES = 5_000;
    private final ArrayDeque<LogEntry> entries = new ArrayDeque<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void save(LogEntry entry) {
        lock.writeLock().lock();
        try {
            entries.addFirst(entry);
            while (entries.size() > MAX_ENTRIES) entries.removeLast();
        } finally { lock.writeLock().unlock(); }
    }

    public List<LogEntry> recent() {
        lock.readLock().lock();
        try { return List.copyOf(entries); }
        finally { lock.readLock().unlock(); }
    }

    public PagedLogs search(String service, CallStatus status, Instant from, Instant to, String requestId, int page, int size) {
        List<LogEntry> filtered = recent().stream()
                .filter(e -> service == null || service.isBlank() || e.serviceId().equalsIgnoreCase(service))
                .filter(e -> status == null || e.status() == status)
                .filter(e -> from == null || !e.timestamp().isBefore(from))
                .filter(e -> to == null || !e.timestamp().isAfter(to))
                .filter(e -> requestId == null || requestId.isBlank() || e.requestId().startsWith(requestId))
                .toList();
        int safePage = Math.max(0, page), safeSize = Math.max(1, Math.min(size, 200));
        int start = (int) Math.min(filtered.size(), (long) safePage * safeSize);
        int end = Math.min(filtered.size(), start + safeSize);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / safeSize);
        return new PagedLogs(filtered.subList(start, end), safePage, safeSize, filtered.size(), totalPages);
    }

    public List<ServiceSummary> summary() {
        Map<String, List<LogEntry>> grouped = recent().stream().collect(Collectors.groupingBy(LogEntry::serviceId));
        return List.of("inventory", "orders", "payments").stream().map(service -> {
            List<LogEntry> calls = grouped.getOrDefault(service, List.of());
            long errors = calls.stream().filter(e -> e.status() == CallStatus.ERROR).count();
            double errorRate = calls.isEmpty() ? 0 : errors * 100.0 / calls.size();
            double average = calls.stream().mapToLong(LogEntry::durationMs).average().orElse(0);
            return new ServiceSummary(service, calls.size(), errorRate, 100 - errorRate, average);
        }).toList();
    }
}
