# Proxy Monitor API

Spring Boot 3 REST API that simulates inventory, order, and payment services. Every service call crosses a hand-written generic `LoggingProxy<T>` which delegates to a real service implementation and stores a bounded audit entry in a thread-safe in-memory repository.

```text
REST controller → LoggingProxy<T> → MicroserviceProxy<T> → real service
                    logs call         shared contract
```

`ProxyConfig` explicitly composes the same proxy class around all three services. The backend does not use Spring AOP, generated proxies, or an interceptor in place of the Proxy pattern.

## Requirements and local run

- Java 17+
- Maven 3.9+

```bash
mvn spring-boot:run
```

The API listens on port `8080` by default. `PORT` can override the port. Set `ALLOWED_ORIGINS` to a comma-separated list of allowed browser origins; the local default is `http://localhost:5173`.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/services/inventory/{operation}` | Call inventory service through its logging proxy |
| POST | `/api/services/orders/{operation}` | Call order service through its logging proxy |
| POST | `/api/services/payments/{operation}` | Call payment service through its logging proxy |
| GET | `/api/metrics/summary` | Per-service calls, error/success rate, and average latency |
| GET | `/api/metrics/logs` | Filtered, paginated audit events |
| POST | `/api/metrics/simulate-load` | Generate 50 service calls |
| GET | `/actuator/health` | Deployment health check |

Service request bodies are optional; pass arguments as `{"params":[...]}`. Example:

```bash
curl -X POST http://localhost:8080/api/services/inventory/checkStock \
  -H "Content-Type: application/json" -d '{"params":["keyboard"]}'
curl "http://localhost:8080/api/metrics/logs?service=payments&status=ERROR&page=0&size=20"
curl -X POST http://localhost:8080/api/metrics/simulate-load
```

Supported operations are `checkStock`, `reserveStock`, `listProducts`; `createOrder`, `getOrder`, `cancelOrder`; and `processPayment`, `refundPayment`, `getPaymentStatus`. Payments fail intentionally at a 10% probability. The simulation uses valid sample records so those intentional failures remain easy to observe.

## Build, tests, and container

```bash
mvn test
mvn package
docker build -t proxy-monitor-api .
```

Unit tests cover proxy success/error behavior, unique request IDs, deterministic payment failure injection, repository filtering/pagination, and service summaries.

## Railway

Connect this repository to a Railway service. Its root contains the multi-stage `Dockerfile`. Configure `ALLOWED_ORIGINS` with the Vercel frontend origin. Railway supplies `PORT`; the health check path is `/actuator/health`.

Backend repository: [monitorio-microservicios-BACKEND](https://github.com/samuelvallejo/monitorio-microservicios-BACKEND)  
Frontend repository: [monitorio-microservicios-FRONTEND](https://github.com/samuelvallejo/monitorio-microservicios-FRONTEND)  
Production URL: pending Railway deployment.

Logs are in memory, limited to the latest 5,000 events, and reset when the application restarts. The microservices are simulations; no external inventory, order, or payment provider is contacted.
