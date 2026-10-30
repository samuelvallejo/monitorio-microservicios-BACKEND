# Microservice Proxy Monitor

Production-ready observability demo with **three independently running Spring Boot microservices**—Inventory, Orders, and Payments—behind a REST gateway. Every gateway call goes through a hand-written generic `LoggingProxy<T>` before it crosses HTTP to the selected microservice. The proxy records the request ID, operation, input, full response or error, status, timestamp, and round-trip time. The React dashboard polls the gateway every three seconds.

## Architecture

```text
React dashboard
      │ REST
      ▼
Spring Boot API gateway
   ├── LoggingProxy<Inventory> ── HTTP ──► Inventory service
   ├── LoggingProxy<Orders>    ── HTTP ──► Orders service
   └── LoggingProxy<Payments>  ── HTTP ──► Payments service
```

Each domain service is a separate executable Maven module and has its own Dockerfile. The services accept authenticated internal requests only. The API gateway is the only public service intended for the dashboard. The Proxy pattern is implemented with concrete Java classes; no Spring AOP, generated proxies, or interceptors are used.

## Local run

Requirements: Java 17+, Maven 3.9+, and Docker Compose.

```bash
docker compose up --build
```

The gateway is available at `http://localhost:8080`; the three service containers listen on ports 8081–8083 inside the Compose network. The local internal token defaults to `local-development-token`. Override it with `INTERNAL_API_TOKEN` when desired.

## Public API

The root URL (`/`) returns a small JSON service index, and `/actuator/health` reports health. Business and metrics operations are available through the endpoints below.

Service requests accept an optional body of the form `{"params":[...]}`. Examples:

```bash
curl -X POST http://localhost:8080/api/services/inventory/checkStock \
  -H "Content-Type: application/json" -d '{"params":["keyboard"]}'

curl -X POST http://localhost:8080/api/services/orders/createOrder \
  -H "Content-Type: application/json" -d '{"params":["keyboard",2]}'

curl -X POST http://localhost:8080/api/services/payments/processPayment \
  -H "Content-Type: application/json" -d '{"params":["ord_demo",49.99]}'

curl http://localhost:8080/api/metrics/summary
curl "http://localhost:8080/api/metrics/logs?service=payments&status=ERROR&page=0&size=20"
curl -X POST http://localhost:8080/api/metrics/simulate-load
curl http://localhost:8080/actuator/health
```

Operations: Inventory (`checkStock`, `reserveStock`, `listProducts`); Orders (`createOrder`, `getOrder`, `cancelOrder`); Payments (`processPayment`, `refundPayment`, `getPaymentStatus`). Payments have a 10% simulated failure rate.

## Deployments

The production dashboard and all four backend services are deployed:

| Component | Production URL |
|---|---|
| Frontend dashboard | [https://monitorio-microservicios-frontend.vercel.app](https://monitorio-microservicios-frontend.vercel.app) |
| API gateway and logging proxy | [https://microservice-audit-monitor-api.onrender.com](https://microservice-audit-monitor-api.onrender.com) |
| Inventory | [https://inventory-service-lbkf.onrender.com](https://inventory-service-lbkf.onrender.com) |
| Orders | [https://orders-service-2e9z.onrender.com](https://orders-service-2e9z.onrender.com) |
| Payments | [https://payments-service-lw6j.onrender.com](https://payments-service-lw6j.onrender.com) |

Deploy each service from this repository's `main` branch as a separate Render Docker web service. Use the repository root as Docker context and select these Dockerfiles:

| Service | Dockerfile | Runtime URL variable on gateway |
|---|---|---|
| Gateway | `Dockerfile` | — |
| Inventory | `services/inventory/Dockerfile` | `INVENTORY_SERVICE_URL` |
| Orders | `services/orders/Dockerfile` | `ORDERS_SERVICE_URL` |
| Payments | `services/payments/Dockerfile` | `PAYMENTS_SERVICE_URL` |

Set `INTERNAL_API_TOKEN` to the same secret value on all four services. Set each gateway URL variable to its corresponding service's base URL, and set `ALLOWED_ORIGINS` on the gateway to the Vercel origin. Never commit the internal token. The gateway health endpoint is `/actuator/health`.

The backend is hosted on Render because the Railway account had reached its free-plan resource limit. Each domain service runs as its own Render service, and the gateway's hand-written logging proxies call them over HTTP. Internal service endpoints require the shared secret configured in Render; it is not stored in this repository. The in-memory audit log retains up to 5,000 entries and resets when the gateway restarts.

## Tests

```bash
mvn test
```

Gateway tests cover proxy success/error logging, request IDs, log filtering/pagination, and summaries. Payment service tests cover the deterministic failure source.
