# log-monitor

A centralized log monitoring system built from 4 Kafka-connected Spring Boot
microservices: clients register/log in via JWT, publish log entries through a
gateway, and two independent consumers persist them and raise alerts — all backed
by MySQL, containerized with Docker, and documented with Swagger/OpenAPI.

See [ARCHITECTURE.md](ARCHITECTURE.md) for diagrams, a request walkthrough, and a
load/scalability discussion.

## Services

| Service | Port | Role | Swagger UI |
|---|---|---|---|
| **auth-service** | 8081 | Register / login, issues RS256 JWTs | http://localhost:8081/swagger-ui/index.html |
| **log-ingestion-service** | 8082 | JWT-secured `POST /api/logs` → publishes to Kafka | http://localhost:8082/swagger-ui/index.html |
| **log-processing-service** | 8083 | Kafka consumer → persists all logs to MySQL, `GET /api/logs` | http://localhost:8083/swagger-ui/index.html |
| **alert-service** | 8084 | Kafka consumer → persists ERROR/CRITICAL as alerts, `GET /api/alerts` | http://localhost:8084/swagger-ui/index.html |

Plus supporting infrastructure: **MySQL** (one instance, 3 logical databases),
**Kafka** (KRaft mode, topic `app-logs` with 3 partitions), and **Kafka UI**
(http://localhost:8080) for inspecting topics/partitions/consumer groups/lag.

## Prerequisites

- Docker + Docker Compose
- (Only needed if building outside Docker) Java 21 and Maven

## Environments

Configuration for **DEV, TEST, STAGE, and PROD** is centralized in one place:
[`env/`](env) — one file per environment (`dev.env`, `test.env`, `stage.env`,
`prod.env`), each setting `SPRING_PROFILES_ACTIVE` plus the shared MySQL
credentials and JWT token lifetime for that environment. Pass the right file to
`docker compose --env-file`, and every one of the 4 services picks it up.

`SPRING_PROFILES_ACTIVE` activates the matching `application-{profile}.yml` in
**each** service (`auth-service`, `log-ingestion-service`,
`log-processing-service`, `alert-service`) — that's what actually changes behavior
per environment:

| | DEV | TEST | STAGE | PROD |
|---|---|---|---|---|
| DB schema handling | `ddl-auto: update` | `ddl-auto: create-drop` (fresh every run) | `ddl-auto: validate` | `ddl-auto: validate` |
| SQL logging | on | off | off | off |
| Log level | DEBUG | INFO | INFO | WARN |
| JWT token lifetime | 1h | 10m | 30m | 15m |
| Swagger UI | enabled | enabled | enabled | **disabled** |

`validate` (stage/prod) means the schema must already exist — those profiles never
auto-create tables, matching how a real environment would be migration-managed
rather than auto-updated. If you point stage/prod at a brand-new empty database,
start it once with `dev` or `test` first so the schema exists, or apply your own
migrations.

### Run a specific environment

```bash
cd log-monitor

# DEV (default if you don't pass --env-file at all)
docker compose --env-file env/dev.env -p log-monitor-dev up --build -d
# or just:
run-dev.bat

# TEST
docker compose --env-file env/test.env -p log-monitor-test up --build -d
run-test.bat

# STAGE
docker compose --env-file env/stage.env -p log-monitor-stage up --build -d
run-stage.bat

# PROD
docker compose --env-file env/prod.env -p log-monitor-prod up --build -d
run-prod.bat
```

The `-p log-monitor-<env>` project name keeps each environment's containers,
network, and volumes separate from the others. Note all environments use the same
host ports (8080–8084, 3307, 9092), so only run **one environment at a time** on a
single machine unless you also customize the port mappings.

First build downloads Maven dependencies + base images, so it takes a few minutes.
Check everything is healthy (swap `-p` for whichever environment you started):

```bash
docker compose -p log-monitor-dev ps
```

Stop an environment with:

```bash
docker compose -p log-monitor-dev down
```

Plain `docker compose up --build -d` (no `--env-file`/`-p`) also still works — it
falls back to `dev`-equivalent defaults baked into `docker-compose.yml`.

## Try it end-to-end

`test-flow.bat` runs the whole flow — register, log in, publish 4 logs (mixed
levels), then query both the processing and alert services to confirm the fan-out
worked:

```bat
test-flow.bat
```

Or manually with curl:

```bash
# Register (or log in if the user already exists)
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123","role":"ADMIN"}'

# Use the returned token for everything below
TOKEN="<paste token here>"

# Publish a log
curl -X POST http://localhost:8082/api/logs \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"serviceName":"order-service","level":"ERROR","message":"Payment failed"}'

# Query persisted logs
curl -H "Authorization: Bearer $TOKEN" http://localhost:8083/api/logs

# Query alerts (ERROR/CRITICAL only)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8084/api/alerts
```

## Load test

`load-test.bat` (PowerShell under the hood) fires a burst of concurrent
`POST /api/logs` calls and reports throughput:

```bat
load-test.bat
:: or with custom volume/concurrency:
powershell -File load-test.ps1 -TotalRequests 300 -Concurrency 30
```

Watch consumer lag drain live in Kafka UI while it runs:
http://localhost:8080/ui/clusters/log-monitor/consumer-groups

In local testing, 200 requests at concurrency 20 completed in ~4.6s (~44 req/s) with
0 failures — see [ARCHITECTURE.md](ARCHITECTURE.md#load--scalability) for what
actually limits throughput and how this scales.

## JWT keys

`keys/` holds a demo RSA keypair used to sign/verify tokens (RS256). `auth-service`
is the only service with the private key; the other three only have the public key.
**These are demo keys checked into the repo for convenience — regenerate your own
before using this anywhere real.**

```bash
openssl genrsa -out keys/private_key_pkcs1.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keys/private_key_pkcs1.pem -out keys/private_key.pem
openssl rsa -in keys/private_key_pkcs1.pem -pubout -out keys/public_key.pem
```

Then copy `private_key.pem` into `auth-service/src/main/resources/keys/` and
`public_key.pem` into the other three services' `src/main/resources/keys/`.

## Notes

- Storage is MySQL, one instance with 3 logical databases (`authdb`, `processingdb`,
  `alertdb`) for demo simplicity — in production these would typically be separate
  instances per service.
- `log-ingestion-service` is intentionally stateless (no database) — it only
  validates JWTs and forwards to Kafka.
- `env/stage.env` and `env/prod.env` contain placeholder passwords checked into the
  repo for demo convenience only — a real deployment must source secrets from a
  secrets manager instead, never a committed file.
