package com.example.proxymonitor.model;

import java.time.Instant;
import java.util.List;

public record LogEntry(String serviceId, String operation, long durationMs, CallStatus status,
                       String requestId, Instant timestamp, List<Object> params, Object response,
                       String errorMessage, String stackTraceSummary) { }
