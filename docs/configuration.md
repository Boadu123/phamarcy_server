# Configuration Guide

Last updated: 2026-07-28.

## Docker Configuration

Docker Compose reads `.env` from the project root. Start from the checked-in example:

```powershell
Copy-Item .env.example .env
```

Current `.env.example` values:

| Variable | Default | Used by | Purpose |
| --- | --- | --- | --- |
| `POSTGRES_DB` | `pharmacy_db` | `db`, `app` | PostgreSQL database name. |
| `POSTGRES_USER` | `postgres` | `db`, `app` | PostgreSQL username. |
| `POSTGRES_PASSWORD` | `postgres` | `db`, `app` | PostgreSQL password. |
| `POSTGRES_PORT` | `5432` | `db` | Host port mapped to PostgreSQL. |
| `SERVER_PORT` | `8080` | `app` | Host and container API port. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | `app` | Comma-separated browser origins permitted to call `/api/v1/**`. |
| `DB_POOL_MAX_SIZE` | `20` | `app` | Hikari maximum pool size. |
| `DB_POOL_MIN_IDLE` | `5` | `app` | Hikari minimum idle connections. |
| `DB_CONNECTION_TIMEOUT_MS` | `30000` | `app` | Hikari connection timeout. |
| `DB_IDLE_TIMEOUT_MS` | `600000` | `app` | Hikari idle timeout. |
| `DB_MAX_LIFETIME_MS` | `1800000` | `app` | Hikari max connection lifetime. |
| `ROOT_LOG_LEVEL` | `INFO` | `app` | Root logging level. |
| `APP_LOG_LEVEL` | `INFO` | `app` | `com.example.phamarcy_server` logging level. |

Inside Docker, Compose sets the app database URL to:

```text
jdbc:postgresql://db:5432/${POSTGRES_DB}
```

The hostname is `db` because that is the Compose service name for PostgreSQL.

## Desktop Client Server URL

The desktop app does not use `DB_URL`. It talks to the Spring Boot API over HTTP and must keep a locally saved backend base URL.

Common values:

| Situation | Desktop backend base URL |
| --- | --- |
| Desktop and Docker server on the same computer | `http://localhost:8080/api/v1` |
| Docker server on another computer in the same network | `http://SERVER-IP-ADDRESS:8080/api/v1` |
| Hosted production server | `https://server.example.com/api/v1` |

The desktop should append endpoint paths to that base URL:

```text
GET  {baseUrl}/health
POST {baseUrl}/sync
```

## Non-Docker Application Settings

`src/main/resources/application.properties` supports these environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | API port. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Comma-separated allowed browser origins. |
| `DB_URL` | `jdbc:postgresql://localhost:5432/pharmacy_db` | JDBC URL. |
| `DB_USERNAME` | `postgres` | Database username. |
| `DB_PASSWORD` | `postgres` | Database password. |
| `DB_POOL_MAX_SIZE` | `20` | Hikari maximum pool size. |
| `DB_POOL_MIN_IDLE` | `5` | Hikari minimum idle connections. |
| `DB_CONNECTION_TIMEOUT_MS` | `30000` | Hikari connection timeout. |
| `DB_IDLE_TIMEOUT_MS` | `600000` | Hikari idle timeout. |
| `DB_MAX_LIFETIME_MS` | `1800000` | Hikari max connection lifetime. |
| `ROOT_LOG_LEVEL` | `INFO` | Root logging level. |
| `APP_LOG_LEVEL` | `INFO` | Application package logging level. |

## Fixed Application Settings

| Setting | Value | Notes |
| --- | --- | --- |
| Flyway | enabled | Migrations run from `classpath:db/migration`. |
| JPA DDL | `validate` | Schema is managed by Flyway, not Hibernate. |
| Jackson naming | `SNAKE_CASE` | Most JSON fields serialize as snake_case. |
| Swagger JSON | `/v3/api-docs` | OpenAPI document endpoint. |
| Swagger UI | `/swagger-ui.html` | Human-readable API UI. |
| Error message exposure | disabled | Spring default error details are not exposed. Custom API errors are returned by `GlobalExceptionHandler`. |
| CORS methods | `GET`, `POST`, `OPTIONS` | Applies only to `/api/v1/**`; credentials are not enabled. |

## Ports

| Port | Service | Notes |
| --- | --- | --- |
| `8080` | Spring Boot app | Change with `SERVER_PORT`. |
| `5432` | PostgreSQL | Change host mapping with `POSTGRES_PORT`. |

## Production Notes

Before production deployment:

- Replace default database credentials.
- Use managed secrets rather than checked-in `.env` files.
- Put the API behind HTTPS.
- Add authentication for sync and admin endpoints.
- Define PostgreSQL backup and restore procedures.
- Avoid exposing the PostgreSQL port publicly.
