package com.example.proxymonitor.model;

public record ServiceSummary(String serviceId, long totalCalls, double errorRate,
                             double successRate, double averageDurationMs) { }
