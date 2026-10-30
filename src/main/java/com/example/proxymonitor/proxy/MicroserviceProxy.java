package com.example.proxymonitor.proxy;

public interface MicroserviceProxy<T> {
    T execute(String operation, Object... params);
}
