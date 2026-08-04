# API Reference

Last verified against the automated HTTP integration suite on 2026-08-04.

## Endpoint Policy

All supported application endpoints are versioned under `/api/v1`.

Do not use non-versioned `/api/...` routes in frontend code, tests, examples, or documentation. Swagger should list only `/api/v1/...` application paths.

Swagger is available at:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

## Security

Current local Docker sync clients do not send an authorization header. The current Spring Security configuration permits these endpoints without authentication:

```text
GET  /api/v1/health
POST /api/v1/sync
GET  /api/v1/admin/**
GET  /v3/api-docs/**
GET  /swagger-ui.html
GET  /swagger-ui/**
```

All other requests are denied. `ApiKeyAuthenticationFilter` and `X-Pharmacy-Token` support remain in the codebase for a future token-auth flow, but the filter is dormant and should not be required by current frontend or desktop clients.

Browser CORS access defaults to `http://localhost:5173` and `http://127.0.0.1:5173`. Configure the comma-separated `CORS_ALLOWED_ORIGINS` environment variable for a different frontend origin. Credentialed CORS requests are not enabled.

Do not expose this server publicly without HTTPS and an authentication plan.

## JSON Rules

- Request and response bodies are JSON.
- Content type for sync requests is `application/json`.
- IDs are UUID strings.
- Timestamps are ISO-8601 instants, for example `2026-07-20T12:00:00Z`.
- Money values accept up to 12 integer digits and 2 decimal places.
- Most JSON fields are snake_case.
- Sync requests use top-level `pharmacyId`; `pharmacy_id` is accepted by the backend, but new clients should send `pharmacyId`.

## Health

```http
GET /api/v1/health
```

Response:

```json
{
  "status": "UP"
}
```

## Synchronization

```http
POST /api/v1/sync
Content-Type: application/json
```

Purpose: accept pending desktop records for one pharmacy and save them in one database transaction.

The server processes records in this order:

```text
users -> products -> batches -> sales -> sale_items -> app_settings
```

This order allows related records in the same payload to resolve correctly. For example, a new product can be sent in the same request as a new batch that references it.

### Request Shape

```json
{
  "pharmacyId": "10000000-0000-0000-0000-000000000001",
  "records": {
    "users": [],
    "products": [],
    "batches": [],
    "sales": [],
    "sale_items": [],
    "app_settings": []
  }
}
```

`records` may be omitted or individual groups may be omitted. Missing groups are treated as empty lists.

### Batch Example

```json
{
  "id": "40000000-0000-0000-0000-000000000001",
  "product_id": "30000000-0000-0000-0000-000000000001",
  "stock_reference": "STK-20260720-A1B2C3D4E5F6",
  "batch_number": "PCM500-26A041",
  "quantity": 120,
  "cost_price": 0.35,
  "selling_price": 0.75,
  "expiry_date": "2028-04-30",
  "sync_status": "PENDING",
  "last_updated_at": "2026-07-20T12:00:00Z"
}
```

`stock_reference` is the unique stock delivery identifier per pharmacy. `batch_number` is the manufacturer lot or batch number and is not unique.

### Response Shape

HTTP `200` means the transaction committed. Desktop clients may mark submitted records as synced only after HTTP `200`.

```json
{
  "pharmacy_id": "10000000-0000-0000-0000-000000000001",
  "users": { "inserted": 1, "updated": 0, "ignored": 0 },
  "products": { "inserted": 1, "updated": 0, "ignored": 0 },
  "batches": { "inserted": 1, "updated": 0, "ignored": 0 },
  "sales": { "inserted": 0, "updated": 0, "ignored": 0 },
  "sale_items": { "inserted": 0, "updated": 0, "ignored": 0 },
  "app_settings": { "inserted": 0, "updated": 0, "ignored": 0 }
}
```

### Sync Semantics

- If the pharmacy does not exist, the server creates a placeholder pharmacy named `Imported Pharmacy {pharmacyId}` with location `Unknown` and token `auto-{pharmacyId}`.
- If a record ID does not exist, the server inserts it.
- If a record ID exists for the same pharmacy and the incoming `last_updated_at` is newer, the server updates it.
- If a record ID exists for the same pharmacy and the incoming `last_updated_at` is equal or older, the server ignores it.
- If a record ID exists for another pharmacy, the server returns `409 Conflict`.
- Duplicate record IDs inside one payload return `400 Bad Request`.
- Duplicate `stock_reference` values for different batch IDs in the same pharmacy return `409 Conflict`.
- Missing relationships return `400 Bad Request`, such as a batch referencing an unknown product.
- Any failure rolls back the whole sync transaction.

### Sync Record Validation

| Record group | Required fields | Notes |
| --- | --- | --- |
| `users` | `id`, `username`, `password_hash`, `role`, `created_at`, `sync_status`, `last_updated_at` | `username` max 100, `password_hash` max 255, `role` max 50. |
| `products` | `id`, `name`, `reorder_level`, `created_at`, `sync_status`, `last_updated_at` | `name` max 255, `category` max 120, `reorder_level` must be 0 or greater. |
| `batches` | `id`, `product_id`, `stock_reference`, `batch_number`, `quantity`, `cost_price`, `selling_price`, `sync_status`, `last_updated_at` | `stock_reference` max 64 and unique per pharmacy, `batch_number` max 120, quantities and prices cannot be negative. |
| `sales` | `id`, `user_id`, `sale_date`, `total_amount`, `sync_status`, `last_updated_at` | `user_id` must resolve to a user in the same pharmacy. |
| `sale_items` | `id`, `sale_id`, `batch_id`, `product_name`, `batch_number`, `quantity_sold`, `unit_price`, `sync_status`, `last_updated_at` | `sale_id` and `batch_id` must resolve in the same pharmacy. |
| `app_settings` | `id`, `setting_key`, `sync_status`, `last_updated_at` | `setting_key` max 150. `setting_value` may be null. |

`sync_status` must be one of:

```text
PENDING
MODIFIED
SYNCED
```

The server validates `sync_status` as part of the incoming contract but does not persist it. Frontend clients still own local queue state.

## Admin Monitoring And Reporting

All admin reads use the detailed records written by `POST /api/v1/sync`. Inventory means active batch rows, and sales means active sale headers. The frontend never asks a user for a UUID: it first loads the pharmacy list and carries the selected `pharmacy_id` internally.

No request body or authentication header is required for any current admin GET endpoint.

### Global Dashboard

```http
GET /api/v1/admin/dashboard
```

This is the primary dashboard request. It combines system totals, synchronization status counts, the latest attempt, and the ten most recent attempts.

```json
{
  "generated_at": "2026-07-28T10:05:00Z",
  "total_pharmacies": 1,
  "pharmacies_with_successful_sync": 1,
  "total_inventory_records": 1,
  "total_units_in_stock": 120,
  "total_inventory_value": 90.00,
  "total_sales_count": 1,
  "total_sales_amount": 25.00,
  "successful_syncs": 3,
  "failed_syncs": 1,
  "in_progress_syncs": 0,
  "latest_sync": {
    "id": "81000000-0000-0000-0000-000000000001",
    "pharmacy_id": "10000000-0000-0000-0000-000000000001",
    "pharmacy_name": "Imported Pharmacy 10000000-0000-0000-0000-000000000001",
    "status": "SUCCESSFUL",
    "started_at": "2026-07-28T10:00:00Z",
    "completed_at": "2026-07-28T10:00:01Z",
    "duration_ms": 1000,
    "records_received": 6,
    "records_inserted": 6,
    "records_updated": 0,
    "records_ignored": 0,
    "inventory_records_received": 2,
    "inventory_records_applied": 2,
    "sales_records_received": 1,
    "sales_records_applied": 1,
    "message": "Synchronization completed successfully"
  },
  "recent_activity": [
    {
      "id": "81000000-0000-0000-0000-000000000001",
      "pharmacy_id": "10000000-0000-0000-0000-000000000001",
      "pharmacy_name": "Imported Pharmacy 10000000-0000-0000-0000-000000000001",
      "status": "SUCCESSFUL",
      "started_at": "2026-07-28T10:00:00Z",
      "completed_at": "2026-07-28T10:00:01Z",
      "duration_ms": 1000,
      "records_received": 6,
      "records_inserted": 6,
      "records_updated": 0,
      "records_ignored": 0,
      "inventory_records_received": 2,
      "inventory_records_applied": 2,
      "sales_records_received": 1,
      "sales_records_applied": 1,
      "message": "Synchronization completed successfully"
    }
  ]
}
```

`latest_sync` is `null` and `recent_activity` is empty before the first validated sync attempt. `pharmacies_with_successful_sync` counts distinct pharmacy IDs with at least one successful audited attempt.

### Pharmacy List

```http
GET /api/v1/admin/pharmacies
```

No parameters. Results are ordered by pharmacy name.

```json
[
  {
    "pharmacy_id": "10000000-0000-0000-0000-000000000001",
    "pharmacy_name": "Imported Pharmacy 10000000-0000-0000-0000-000000000001",
    "location": "Unknown",
    "sync_status": "SUCCESSFUL",
    "last_sync_at": "2026-07-28T10:00:01Z",
    "total_inventory_records": 1,
    "total_units_in_stock": 120,
    "total_inventory_value": 90.00,
    "total_sales_count": 1,
    "total_sales_amount": 25.00
  }
]
```

Possible `sync_status` values are `NEVER_SYNCED`, `IN_PROGRESS`, `SUCCESSFUL`, and `FAILED`. `last_sync_at` is absent when no audit exists.

### Pharmacy Details

```http
GET /api/v1/admin/pharmacies/{pharmacyId}
```

`pharmacyId` is required in the path, but it comes from the selected pharmacy-list item and must not be entered by the user.

```json
{
  "pharmacy_id": "10000000-0000-0000-0000-000000000001",
  "pharmacy_name": "Imported Pharmacy 10000000-0000-0000-0000-000000000001",
  "location": "Unknown",
  "sync_status": "SUCCESSFUL",
  "last_sync_at": "2026-07-28T10:00:01Z",
  "inventory": {
    "total_records": 1,
    "total_units_in_stock": 120,
    "total_value": 90.00
  },
  "sales": {
    "total_transactions": 1,
    "total_amount": 25.00
  },
  "successful_syncs": 3,
  "failed_syncs": 1,
  "latest_sync": {
    "id": "81000000-0000-0000-0000-000000000001",
    "pharmacy_id": "10000000-0000-0000-0000-000000000001",
    "pharmacy_name": "Imported Pharmacy 10000000-0000-0000-0000-000000000001",
    "status": "SUCCESSFUL",
    "started_at": "2026-07-28T10:00:00Z",
    "completed_at": "2026-07-28T10:00:01Z",
    "duration_ms": 1000,
    "records_received": 6,
    "records_inserted": 6,
    "records_updated": 0,
    "records_ignored": 0,
    "inventory_records_received": 2,
    "inventory_records_applied": 2,
    "sales_records_received": 1,
    "sales_records_applied": 1,
    "message": "Synchronization completed successfully"
  },
  "recent_activity": [
    {
      "id": "81000000-0000-0000-0000-000000000001",
      "pharmacy_id": "10000000-0000-0000-0000-000000000001",
      "pharmacy_name": "Imported Pharmacy 10000000-0000-0000-0000-000000000001",
      "status": "SUCCESSFUL",
      "started_at": "2026-07-28T10:00:00Z",
      "completed_at": "2026-07-28T10:00:01Z",
      "duration_ms": 1000,
      "records_received": 6,
      "records_inserted": 6,
      "records_updated": 0,
      "records_ignored": 0,
      "inventory_records_received": 2,
      "inventory_records_applied": 2,
      "sales_records_received": 1,
      "sales_records_applied": 1,
      "message": "Synchronization completed successfully"
    }
  ]
}
```

`latest_sync` and each `recent_activity` entry use the synchronization activity shape shown in the global dashboard response. The recent list contains up to ten entries.

### Synchronization Activity

```http
GET /api/v1/admin/sync-activity?limit=20
GET /api/v1/admin/pharmacies/{pharmacyId}/sync-activity?limit=20
```

`limit` is optional, defaults to `20`, and must be from `1` through `100`. Results are newest first. A pharmacy-specific request returns `404` when the pharmacy is unknown.

Activity status values:

- `IN_PROGRESS`: the validated request reached sync processing and has not completed.
- `SUCCESSFUL`: the complete operational transaction committed.
- `FAILED`: sync processing failed and the operational transaction rolled back.

Auditing is deliberately best-effort and separate from the operational transaction. An audit-storage problem never changes a successfully committed desktop response from HTTP `200`. Request-body validation failures rejected before controller processing are returned as HTTP `400` but are not written as activity rows.

`records_received` is the number of rows in all submitted groups. Applied counts include inserts plus strictly-newer updates; ignored records are reported separately. Inventory received/applied counts combine products and batches. Sales counts represent sale headers, not sale items.

### Compatibility Pharmacy Dashboard

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/dashboard
```

This existing compact endpoint remains available and now reads the detailed tables. New frontends should normally use the comprehensive pharmacy-details endpoint.

### Pharmacy Inventory

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/inventory
```

Returns active detailed batch rows ordered by product and stock reference:

```json
[
  {
    "id": "40000000-0000-0000-0000-000000000001",
    "pharmacy_id": "10000000-0000-0000-0000-000000000001",
    "product_id": "30000000-0000-0000-0000-000000000001",
    "product_name": "Paracetamol",
    "category": "Medicine",
    "stock_reference": "STK-20260728-A1B2C3D4E5F6",
    "batch_number": "PCM500-26A041",
    "quantity": 120,
    "cost_price": 0.35,
    "selling_price": 0.75,
    "inventory_value": 90.00,
    "expiry_date": "2028-04-30",
    "last_updated_at": "2026-07-28T10:00:00Z"
  }
]
```

### Pharmacy Sales

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/sales
```

Returns active detailed sale headers newest first:

```json
[
  {
    "id": "50000000-0000-0000-0000-000000000001",
    "pharmacy_id": "10000000-0000-0000-0000-000000000001",
    "user_id": "20000000-0000-0000-0000-000000000001",
    "username": "cashier",
    "total_amount": 25.00,
    "sale_date": "2026-07-28T10:00:00Z",
    "item_count": 1,
    "last_updated_at": "2026-07-28T10:00:00Z"
  }
]
```

### Pharmacy Sale Details

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/sales/{saleId}
```

Returns one active sale only when it belongs to the pharmacy in the same path. The ownership constraint is applied by the database query; a sale belonging to another pharmacy is returned as `404 Not Found`, just like an unknown sale.

```json
{
  "id": "50000000-0000-0000-0000-000000000001",
  "pharmacy_id": "10000000-0000-0000-0000-000000000001",
  "pharmacy_name": "Example Pharmacy",
  "location": "Reykjavik",
  "user_id": "20000000-0000-0000-0000-000000000001",
  "username": "cashier",
  "total_amount": 48.25,
  "sale_date": "2026-08-04T09:20:00Z",
  "item_count": 2,
  "created_at": "2026-08-04T09:00:00Z",
  "last_updated_at": "2026-08-04T09:30:00Z",
  "items": [
    {
      "id": "60000000-0000-0000-0000-000000000001",
      "product_id": "30000000-0000-0000-0000-000000000001",
      "product_name": "Paracetamol",
      "batch_id": "40000000-0000-0000-0000-000000000001",
      "stock_reference": "STK-PARA-001",
      "batch_number": "PARA-26A",
      "quantity_sold": 2,
      "unit_price": 12.50,
      "subtotal": 25.00,
      "created_at": "2026-08-04T09:00:00Z",
      "last_updated_at": "2026-08-04T09:30:00Z"
    },
    {
      "id": "60000000-0000-0000-0000-000000000002",
      "product_id": "30000000-0000-0000-0000-000000000002",
      "product_name": "ORS",
      "batch_id": "40000000-0000-0000-0000-000000000002",
      "stock_reference": "STK-ORS-002",
      "batch_number": "ORS-26B",
      "quantity_sold": 3,
      "unit_price": 7.75,
      "subtotal": 23.25,
      "created_at": "2026-08-04T09:00:01Z",
      "last_updated_at": "2026-08-04T09:30:00Z"
    }
  ]
}
```

`total_amount` is the authoritative stored sale total. Each item `subtotal` is calculated with decimal arithmetic as `quantity_sold * unit_price`. Product and manufacturer batch names come from the sale-item snapshots; `product_id` and `stock_reference` are resolved through the associated batch. Deleted sale items are omitted. A valid sale with no active items returns `"item_count": 0` and `"items": []`.

Invalid `pharmacyId` or `saleId` values return the standard validation body with HTTP `400`. An unknown pharmacy returns the pharmacy `404`. An unknown sale and a sale owned by another pharmacy return the same scoped sale `404`. The endpoint follows the current admin-read security policy described above, so no authentication header is required in the current configuration.

## Error Format

Validation and application errors use this shape:

```json
{
  "timestamp": "2026-07-21T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/sync",
  "violations": [
    { "field": "records.batches[0].stockReference", "message": "must not be blank" }
  ]
}
```

Common statuses:

| Status | Meaning |
| --- | --- |
| `200` | Request succeeded. Sync committed. |
| `400` | Malformed JSON, validation failure (including invalid UUID path values), duplicate ID in payload, or missing relationship. |
| `404` | Pharmacy or pharmacy-scoped sale was not found, or the requested application route does not exist. |
| `409` | Record belongs to another pharmacy or batch `stock_reference` conflicts. |
| `500` | Unexpected server error. |
