# Pharmacy Server

Dockerized Spring Boot backend for offline-first pharmacy synchronization and branch reporting.

The server accepts pharmacy desktop sync payloads, stores branch-scoped records in PostgreSQL, and exposes read-only admin reporting endpoints. Docker Compose is the default way to run the project locally because it starts both PostgreSQL and the Spring Boot API with matching configuration.

## Current API Rule

Use only versioned endpoints under `/api/v1`.

The non-versioned `/api/...` endpoints are deprecated and must not be used in frontend integration, tests, examples, or new documentation. Swagger currently advertises only `/api/v1/...` paths.

## Quick Start With Docker

From the project root:

```powershell
cd C:\Users\bboad\Desktop\phamarcy_server
docker compose up --build -d
```

Open:

```text
Health:  http://localhost:8080/api/v1/health
Swagger: http://localhost:8080/swagger-ui.html
```

## Desktop Sync URL

The desktop application must store the server API root URL in its own local configuration before it can sync. The backend cannot tell the desktop this URL automatically, because the desktop needs the URL first in order to contact the backend.

Use this value when the desktop and Docker server run on the same computer:

```text
http://localhost:8080/api/v1
```

Use this shape when the server runs on another computer on the same network:

```text
http://SERVER-IP-ADDRESS:8080/api/v1
```

For a hosted server, use the real HTTPS domain:

```text
https://server.example.com/api/v1
```

The desktop should save that value locally, then call `GET {baseUrl}/health` and `POST {baseUrl}/sync`.

Stop the containers:

```powershell
docker compose down
```

Delete the local Docker database volume only when you intentionally want to erase local data:

```powershell
docker compose down -v
```

## Main Endpoints

```text
GET  /api/v1/health
POST /api/v1/sync
GET  /api/v1/admin/dashboard
GET  /api/v1/admin/pharmacies
GET  /api/v1/admin/pharmacies/{pharmacyId}
GET  /api/v1/admin/sync-activity?limit=20
GET  /api/v1/admin/pharmacies/{pharmacyId}/sync-activity?limit=20
GET  /api/v1/admin/pharmacies/{pharmacyId}/dashboard
GET  /api/v1/admin/pharmacies/{pharmacyId}/inventory
GET  /api/v1/admin/pharmacies/{pharmacyId}/sales
```

See [docs/api.md](docs/api.md) for payloads, validation rules, response examples, and error formats.

## Project Structure

```text
src/main/java/com/example/phamarcy_server
  config/       Spring Security and OpenAPI configuration
  controller/   Versioned HTTP endpoints
  dto/          Request and response contracts
  entity/       JPA entities mapped to Flyway-managed tables
  exception/    API error mapping
  repository/   Spring Data repositories
  security/     Dormant API-token filter support
  service/      Sync and admin reporting business logic
src/main/resources/db/migration
  V1__create_pharmacy_sync_schema.sql
  V2__add_batch_stock_reference.sql
  V3__add_sync_activity.sql
```

## Important Documentation

- [DOCKER.md](DOCKER.md): Docker run, logs, stop, and data-volume commands.
- [docs/api.md](docs/api.md): Versioned API reference and Swagger policy.
- [docs/architecture.md](docs/architecture.md): Runtime architecture, sync sequence, audit flow, and detailed reporting.
- [docs/database-schema.md](docs/database-schema.md): Tables, relationships, indexes, and migrations.
- [docs/configuration.md](docs/configuration.md): Environment variables and runtime configuration.
- [docs/testing.md](docs/testing.md): Test commands, smoke checks, and doc verification.
- [docs/frontend-ui-spec.md](docs/frontend-ui-spec.md): Frontend-facing screen and behavior specification.
- [docs/frontend-dashboard-prompt.md](docs/frontend-dashboard-prompt.md): Ready-to-use implementation prompt for the monitoring frontend.

## Local Development Without Docker

The application can run directly with Maven if PostgreSQL is already available:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/pharmacy_db'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='postgres'
.\mvnw.cmd spring-boot:run
```

Docker remains the preferred path because it keeps the app and database configuration aligned.

## Testing

Run the test suite:

```powershell
.\mvnw.cmd -q clean test
```

Package without rerunning tests:

```powershell
.\mvnw.cmd -q -DskipTests package
```

Tests use H2 in PostgreSQL compatibility mode and run Flyway migrations from `src/main/resources/db/migration`.

## Documentation Maintenance

Treat docs as part of every implementation change. When code changes, update the matching API docs, Docker/config docs, database docs, frontend spec, test notes, and Swagger annotations in the same change.
