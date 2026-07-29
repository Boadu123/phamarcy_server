# Running With Docker

Docker Compose is the preferred local runtime for this backend. It starts two containers:

- `db`: PostgreSQL, where synced and reporting data is stored.
- `app`: the Spring Boot pharmacy sync server.

The app talks to PostgreSQL through the Docker service name `db`, not `localhost`.

## Prerequisite

Install and start Docker Desktop, or another Docker engine that includes Docker Compose.

## Start The Application

From PowerShell in the project root:

```powershell
cd C:\Users\bboad\Desktop\phamarcy_server
docker compose up --build -d
```

On the first run, Docker will:

1. Download PostgreSQL if it is not already on the machine.
2. Build the Java application image.
3. Start PostgreSQL.
4. Wait until PostgreSQL is healthy.
5. Start the pharmacy server.
6. Run Flyway migrations to create or upgrade database tables.

The server will be available at:

```text
Health:  http://localhost:8080/api/v1/health
Sync:    http://localhost:8080/api/v1/sync
Swagger: http://localhost:8080/swagger-ui.html
```

Use only the versioned `/api/v1/...` routes. The unversioned `/api/...` routes are deprecated and should not be used for frontend integration, tests, or examples.

## Desktop App Server URL

The desktop app needs a locally saved backend base URL. It should not connect directly to PostgreSQL and it should not guess a URL silently.

Use this when the desktop app and Docker server are on the same computer:

```text
http://localhost:8080/api/v1
```

Use this when the Docker server is on another computer in the pharmacy or office network:

```text
http://SERVER-IP-ADDRESS:8080/api/v1
```

For example, if the server computer's network IP is `192.168.1.25`, the desktop base URL is:

```text
http://192.168.1.25:8080/api/v1
```

The desktop then checks `GET {baseUrl}/health` and syncs with `POST {baseUrl}/sync`.

## View Logs

```powershell
docker compose logs -f app
```

PostgreSQL logs:

```powershell
docker compose logs -f db
```

## Check Status

```powershell
docker compose ps
```

Expected services:

```text
pharmacy-db       Up ... (healthy)
pharmacy-server   Up ... 0.0.0.0:8080->8080/tcp
```

## Stop The Application

Stop containers but keep database data:

```powershell
docker compose down
```

Stop containers and delete the PostgreSQL data volume:

```powershell
docker compose down -v
```

Only use `docker compose down -v` when you intentionally want to erase the local Docker database.

## Configuration

Compose provides local defaults. To customize them, copy `.env.example` to `.env` and change the values.

PowerShell:

```powershell
Copy-Item .env.example .env
```

Useful settings:

| Variable | Default | Purpose |
| --- | --- | --- |
| `POSTGRES_DB` | `pharmacy_db` | Database name. |
| `POSTGRES_USER` | `postgres` | Database username. |
| `POSTGRES_PASSWORD` | `postgres` | Database password. |
| `POSTGRES_PORT` | `5432` | Host port mapped to PostgreSQL. |
| `SERVER_PORT` | `8080` | Host and container API port. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Browser origins allowed to call the versioned API. |
| `DB_POOL_MAX_SIZE` | `20` | App database pool maximum size. |
| `DB_POOL_MIN_IDLE` | `5` | App database pool minimum idle connections. |
| `DB_CONNECTION_TIMEOUT_MS` | `30000` | App database connection timeout. |
| `DB_IDLE_TIMEOUT_MS` | `600000` | App database idle timeout. |
| `DB_MAX_LIFETIME_MS` | `1800000` | App database max connection lifetime. |
| `ROOT_LOG_LEVEL` | `INFO` | Root logging level. |
| `APP_LOG_LEVEL` | `INFO` | Application package logging level. |

Inside Docker, the application connects to PostgreSQL using:

```text
jdbc:postgresql://db:5432/pharmacy_db
```

See [docs/configuration.md](docs/configuration.md) for the full configuration reference.

## Sync Flow In Plain English

1. The desktop pharmacy app checks `GET /api/v1/health` to see if the server is awake.
2. If the health check succeeds, it sends changed records to `POST /api/v1/sync`.
3. The server reads the `pharmacyId` from the request.
4. If that pharmacy is new to this server, the server creates a placeholder pharmacy row.
5. The server saves the records in this order: users, products, batches, sales, sale items, app settings.
6. If every record saves correctly, the server commits the database transaction and returns HTTP 200.
7. If anything fails, the whole transaction rolls back and the desktop should try again later.

The server accepts UUID string IDs from the desktop. For batches, it stores `stock_reference` separately from the manufacturer's `batch_number`, so two stock deliveries can share the same manufacturer lot number without being merged.

## Build Only

Build the application image without starting it:

```powershell
docker compose build app
```

## Rebuild After Code Changes

```powershell
docker compose up -d --build --force-recreate app
```

Then smoke check:

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/health'
```

## More Documentation

- [README.md](README.md): Project overview.
- [docs/api.md](docs/api.md): API reference.
- [docs/architecture.md](docs/architecture.md): Architecture and flow diagrams.
- [docs/database-schema.md](docs/database-schema.md): Database schema.
- [docs/testing.md](docs/testing.md): Test and smoke-check commands.
