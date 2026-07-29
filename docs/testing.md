# Testing Guide

Last updated: 2026-07-28.

## Test Stack

The automated tests use:

- JUnit and Spring Boot test support.
- MockMvc for HTTP endpoint checks.
- H2 in PostgreSQL compatibility mode.
- Flyway migrations from `src/main/resources/db/migration`.
- JPA schema validation through Hibernate.

Test properties live in:

```text
src/test/resources/application.properties
```

## Main Commands

Run all tests from a clean build:

```powershell
.\mvnw.cmd -q clean test
```

Package the application without rerunning tests:

```powershell
.\mvnw.cmd -q -DskipTests package
```

Build and run the Dockerized app:

```powershell
docker compose up -d --build --force-recreate app
```

Check running containers:

```powershell
docker compose ps
```

View app logs:

```powershell
docker compose logs -f app
```

## API Smoke Checks

Health endpoint:

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/health'
```

Swagger path check:

```powershell
$openApi = Invoke-RestMethod -Uri 'http://localhost:8080/v3/api-docs'
$openApi.paths.PSObject.Properties.Name | Sort-Object
```

Expected application paths:

```text
/api/v1/admin/dashboard
/api/v1/admin/pharmacies
/api/v1/admin/pharmacies/{pharmacyId}
/api/v1/admin/pharmacies/{pharmacyId}/dashboard
/api/v1/admin/pharmacies/{pharmacyId}/inventory
/api/v1/admin/pharmacies/{pharmacyId}/sales
/api/v1/admin/pharmacies/{pharmacyId}/sync-activity
/api/v1/admin/sync-activity
/api/v1/health
/api/v1/sync
```

There should be no non-versioned application paths in Swagger.

## Current Automated Coverage

`SyncServiceIntegrationTests` verifies:

- `/api/v1/health` is open and returns `UP`.
- `/api/v1/sync` accepts the current desktop payload without `X-Pharmacy-Token`.
- Full sync inserts detailed users, products, batches, sales, sale items, and app settings.
- Re-sending the same payload is idempotent and counts rows as ignored.
- Newer `last_updated_at` values update existing records.
- Repeated manufacturer `batch_number` values are allowed when `stock_reference` values differ.

`PhamarcyServerApplicationTests` verifies the Spring application context starts.

`AdminReportingIntegrationTests` verifies:

- A representative HTTP sync populates dashboard, pharmacy list, details, inventory, sales, and activity responses from detailed records.
- Aggregate quantities, inventory value, sales amount, and applied sync counts are correct.
- A missing relationship rolls operational changes back and persists a frontend-visible failed attempt.
- Activity limits and pharmacy-not-found errors use the shared JSON error contract.
- The configured localhost frontend origin receives a valid CORS preflight response.

## Documentation Verification

Search for unversioned endpoint examples before finishing API or frontend work:

```powershell
rg -P '/api/(?!v1)' README.md DOCKER.md docs src
```

Allowed matches should only be warnings that unversioned routes are deprecated and must not be used.

Search for legacy route constants:

```powershell
rg 'LEGACY_|ApiPaths\.API' src
```

Expected result: no matches.

## When Adding Features

For every endpoint, schema, or sync behavior change:

1. Add or update focused automated tests.
2. Update Swagger annotations if the HTTP contract changes.
3. Update `docs/api.md` and any affected architecture, database, frontend, configuration, or testing docs.
4. Run the test suite.
5. If Docker behavior changed, rebuild and smoke check the running container.
