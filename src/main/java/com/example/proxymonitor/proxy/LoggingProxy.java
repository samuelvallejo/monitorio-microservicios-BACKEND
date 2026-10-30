package com.example.proxymonitor.proxy;

import com.example.proxymonitor.model.CallStatus;
import com.example.proxymonitor.model.LogEntry;
import com.example.proxymonitor.repository.LogRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class LoggingProxy<T> implements MicroserviceProxy<T> {
    private final String serviceId;
    private final MicroserviceProxy<T> delegate;
    private final LogRepository repository;
    private final LongSupplier nanoTime;

    public LoggingProxy(String serviceId, MicroserviceProxy<T> delegate, LogRepository repository) {
        this(serviceId, delegate, repository, System::nanoTime);
    }

    LoggingProxy(String serviceId, MicroserviceProxy<T> delegate, LogRepository repository, LongSupplier nanoTime) {
        this.serviceId = serviceId;
        this.delegate = delegate;
        this.repository = repository;
        this.nanoTime = nanoTime;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        long started = nanoTime.getAsLong();
        List<Object> safeParams = params == null ? List.of() : Arrays.asList(params.clone());
        System.out.printf("[%s] request started service=%s operation=%s%n", requestId, serviceId, operation);
        try {
            T response = delegate.execute(operation, params == null ? new Object[0] : params);
            long duration = elapsedMillis(started);
            repository.save(new LogEntry(serviceId, operation, duration, CallStatus.SUCCESS, requestId,
                    timestamp, safeParams, response, null, null));
            System.out.printf("[%s] request completed status=SUCCESS durationMs=%d%n", requestId, duration);
            return response;
        } catch (RuntimeException exception) {
            long duration = elapsedMillis(started);
            StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));
            String summary = writer.toString().lines().limit(5).reduce((a, b) -> a + "\n" + b).orElse("");
            repository.save(new LogEntry(serviceId, operation, duration, CallStatus.ERROR, requestId,
                    timestamp, safeParams, null, exception.getMessage(), summary));
            System.err.printf("[%s] request completed status=ERROR durationMs=%d message=%s%n",
                    requestId, duration, exception.getMessage());
            throw exception;
        }
    }

    private long elapsedMillis(long started) { return Math.max(0, (nanoTime.getAsLong() - started) / 1_000_000); }
}
