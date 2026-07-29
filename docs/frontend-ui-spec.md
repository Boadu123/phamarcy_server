# Frontend UI Specification: Dockerized Pharmacy Sync Updates

Last updated: 2026-07-21

## Purpose

This document describes the frontend changes required after the server was Dockerized and the sync contract was updated. The frontend should treat the backend as a Dockerized HTTP service. It should not connect directly to PostgreSQL and should not assume Java or database services are installed outside Docker.

The new server-facing sync contract is:

- Health check: `GET /api/v1/health`
- Sync submit: `POST /api/v1/sync`
- Content type: `application/json`
- Current auth: no authorization header for `/api/v1/sync`
- Top-level pharmacy field: `pharmacyId`
- Record fields: lowercase snake_case inside `records`
- Record IDs and foreign keys: UUID strings
- Empty record groups may be omitted
- Success: only HTTP `200` means the desktop can mark submitted rows as synced
- Failure: any non-200 means keep submitted rows pending and retry later

Use only the versioned `/api/v1/...` routes. Treat unversioned `/api/...` endpoints as deprecated and do not reference or implement them.

Related backend docs: [API Reference](api.md), [Architecture And Flow](architecture.md), [Database Schema](database-schema.md), and [Configuration Guide](configuration.md).

## Shared API Contract

### Base URL

The configured backend URL should point to the API root, for example:

```text
http://localhost:8080/api/v1
https://server.example.com/api/v1
```

The UI should normalize a trailing slash so both of these work:

```text
http://localhost:8080/api/v1
http://localhost:8080/api/v1/
```

The desktop must persist this value in local client configuration before the first sync. Do not store the server URL only in synced `app_settings`, because the app needs the URL before it can reach the server. A local-only setting such as `backend_base_url` is acceptable.

First-run behavior:

- If no backend base URL is saved, show the Server Connection setup screen before automatic sync starts.
- Default to `http://localhost:8080/api/v1` only when the server is expected to run on the same computer.
- If the server runs on another machine, the user must enter that machine's IP address or HTTPS domain with `/api/v1` at the end.

### Health Check

```http
GET {baseUrl}/health
```

Expected success:

```json
{
  "status": "UP"
}
```

Behavior:

- Treat any 2xx response as online.
- Use a 5 second timeout.
- Do not require authentication.

### Sync Submit

```http
POST {baseUrl}/sync
Content-Type: application/json
```

Request shape:

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

The frontend may omit empty groups:

```json
{
  "pharmacyId": "10000000-0000-0000-0000-000000000001",
  "records": {
    "products": [],
    "batches": []
  }
}
```

Response shape:

```json
{
  "pharmacyId": "10000000-0000-0000-0000-000000000001",
  "users": { "inserted": 1, "updated": 0, "ignored": 0 },
  "products": { "inserted": 1, "updated": 0, "ignored": 0 },
  "batches": { "inserted": 1, "updated": 0, "ignored": 0 },
  "sales": { "inserted": 0, "updated": 0, "ignored": 0 },
  "saleItems": { "inserted": 0, "updated": 0, "ignored": 0 },
  "appSettings": { "inserted": 0, "updated": 0, "ignored": 0 }
}
```

The current desktop client may ignore the response body. A management UI may display the counts.

### Error Shape

Backend errors use this shape:

```json
{
  "timestamp": "2026-07-20T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/sync",
  "violations": [
    { "field": "records.batches[0].stockReference", "message": "must not be blank" }
  ]
}
```

The UI should show a user-friendly summary and keep technical details available in an expandable panel or diagnostic log.

## Screen 1: Backend Connection Settings

### Purpose

Let the user configure where the Dockerized server is running and confirm that the desktop can reach it.

### Components To Add Or Modify

- Backend base URL input, stored locally as client configuration before the first sync.
- Test Connection button.
- Connection status badge: Untested, Online, Offline, Checking.
- Last checked timestamp.
- Help text with examples:
  - Local Docker: `http://localhost:8080/api/v1`
  - Hosted server: `https://server.example.com/api/v1`
- Remove or hide any required `X-Pharmacy-Token` input for the current client flow.
- Production warning if the URL is plain `http://` and not localhost.

### Interaction And Behavior

- When the user clicks Test Connection, call `GET {baseUrl}/health`.
- Show Checking while the request is pending.
- Show Online for any 2xx response.
- Show Offline for timeout, network error, or non-2xx response.
- Save the normalized URL after a successful test, or allow manual save with a warning.
- The desktop should run a health check on app startup before the first sync attempt.

### Validation Rules

- URL is required.
- URL must start with `http://` or `https://`.
- URL should end at `/api/v1`, not `/api/v1/sync` or `/api/v1/health`.
- Timeout for health check is 5 seconds.
- HTTPS should be required or strongly warned for non-local production deployments.

### API Required

- `GET /api/v1/health`

### Success States

- Status badge says Online.
- Last checked timestamp updates.
- Save button confirms the base URL has been stored locally and will be used for future health checks and sync attempts.

### Error States

- Cannot reach server.
- Server returned non-2xx.
- URL format invalid.
- Timeout after 5 seconds.
- Docker Desktop not running locally, likely shown as connection refused.

### Edge Cases

- User enters `http://localhost:8080` without `/api/v1`: suggest `http://localhost:8080/api/v1`.
- User enters trailing slash: normalize silently.
- Server starts slowly after Docker Compose: allow retry without leaving the screen.
- Offline mode: keep local work available and leave sync records pending.

### Accessibility And Usability

- Status must not rely on color only. Pair color with text and icon.
- The Test Connection button must have a clear loading state.
- Error messages should explain the next action, such as Start Docker Desktop or check the URL.

## Screen 2: Sync Status And Sync Activity

### Purpose

Show the user whether local records are waiting to be sent, whether sync is running, and whether the last sync succeeded.

### Components To Add Or Modify

- Sync status indicator: Idle, Syncing, Synced, Offline, Failed.
- Pending record counts by group:
  - Users
  - Products
  - Batches
  - Sales
  - Sale items
  - App settings
- Last successful sync timestamp.
- Last failed sync timestamp and short reason.
- Next automatic sync timestamp.
- Manual Sync Now button.
- Optional diagnostics drawer showing the latest response status and API error message.

### Interaction And Behavior

- Run sync immediately after startup when online.
- Run sync every 5 minutes after that.
- Manual Sync Now triggers the same flow and should be disabled while sync is already running.
- Before sending, call `GET {baseUrl}/health`, where `baseUrl` ends with `/api/v1`.
- Send only records with local sync status `PENDING` or `MODIFIED`.
- Omit empty record groups from the request.
- On HTTP 200, mark every submitted row as `SYNCED` locally.
- On any non-200 or network failure, do not mark submitted rows as synced.

### Validation Rules

- `pharmacyId` must be a UUID string.
- All record IDs and foreign keys must be UUID strings.
- `sync_status` must be one of `PENDING`, `MODIFIED`, or `SYNCED`.
- Timestamps must be ISO-8601 UTC strings, for example `2026-07-20T12:00:00Z`.

### API Required

- `GET /api/v1/health`
- `POST /api/v1/sync`

### Success States

- Show Synced after HTTP 200.
- Reset pending counts for submitted records to zero.
- Optionally show inserted, updated, ignored counts from the response.

### Error States

- Network error: show Offline and keep records pending.
- Health timeout: show Offline and retry later.
- Sync timeout after 20 seconds: show Failed and keep records pending.
- HTTP 400: show validation problem and keep records pending.
- HTTP 409: show conflict problem and keep records pending.
- HTTP 500: show server problem and keep records pending.

### Edge Cases

- Empty queue: do not send a sync request unless a manual debug action needs to verify the endpoint.
- Duplicate request retry: retrying the same UUID should be safe. UI should not create duplicate local records on retry.
- App closes during sync: keep records pending until HTTP 200 is confirmed.
- Server returns 200 but response body is unreadable: current rule still allows marking rows synced because HTTP 200 is authoritative.

### Accessibility And Usability

- Keep sync status visible but quiet. Do not interrupt sales workflows for routine sync success.
- Make failed sync details available without blocking local work.
- Use readable timestamps such as Today 12:05 PM, with full timestamp in tooltip or details.

## Screen 3: First Sync After Upgrade Or Migration

### Purpose

Prevent accidental duplicate imports when an upgraded desktop app sends old historical data with newly generated UUIDs.

### Components To Add Or Modify

- First Sync Review dialog or banner shown after upgrade when migrated records are pending.
- Summary of pending record counts by group.
- Warning text explaining that the first sync may send historical records again.
- Backup confirmation checkbox for production environments.
- Proceed With Sync button.
- Sync Later button.

### Interaction And Behavior

- Show this warning before the first sync after migration if historical records are marked `PENDING`.
- Allow the user to defer sync and keep using the desktop offline.
- Require explicit confirmation before syncing to a production server if many historical rows are pending.

### Validation Rules

- Do not proceed if backend URL is missing or health check fails.
- Do not proceed if `pharmacyId` is missing or not a UUID.

### API Required

- `GET /api/v1/health`
- `POST /api/v1/sync` after confirmation

### Success States

- After HTTP 200, close the dialog and show Synced.

### Error States

- Non-200 response: keep dialog available with retry option.
- Conflict response: tell user to contact admin or run reconciliation before retrying.

### Edge Cases

- Server is empty: first import should be allowed.
- Server already contains old integer-ID data: blind import may duplicate business records. UI should encourage backup and admin review.
- User chooses Sync Later: do not nag constantly. Show a persistent but non-blocking banner.

### Accessibility And Usability

- Use plain language, not database jargon.
- Make the destructive-risk warning clear without implying data has already been changed.
- Confirmation checkbox label should describe the action, not just say I agree.

## Screen 4: Stock Receiving And Batch Form

### Purpose

Separate the pharmacy-generated stock reference from the manufacturer batch or lot number. The server now treats `stock_reference` as the unique stock delivery identifier and allows duplicate manufacturer batch numbers.

### Components To Add Or Modify

- Add Stock Reference field.
- Keep Manufacturer Batch/Lot Number field.
- Product selector remains required.
- Quantity field remains required.
- Cost price field remains required.
- Selling price field remains required.
- Expiry date field remains optional.
- Add read-only UUID diagnostics only if the app already has developer/admin diagnostics.
- Remove any validation or error copy that says manufacturer batch number must be unique.

Recommended labels:

- Stock Reference: internal delivery reference, for example `STK-20260720-A1B2C3D4E5F6`
- Manufacturer Batch Number: number printed on package, for example `PCM500-26A041`

### Interaction And Behavior

- Generate `stock_reference` locally when a new stock delivery is created.
- Do not wait for the server to generate or replace it. The server will not do that.
- Allow the same manufacturer batch number to be used on multiple stock deliveries.
- Use `stock_reference` and the row UUID to identify a delivery in the UI.
- Editing stock details should mark the batch record `MODIFIED`.

### Validation Rules

- `id`: required UUID string.
- `product_id`: required UUID string.
- `stock_reference`: required, max 64 characters, unique per pharmacy.
- `batch_number`: required, max 120 characters, not unique.
- `quantity`: required integer, minimum 0.
- `cost_price`: required decimal, minimum 0.00, max 12 integer digits and 2 fractional digits.
- `selling_price`: required decimal, minimum 0.00, max 12 integer digits and 2 fractional digits.
- `expiry_date`: optional, `YYYY-MM-DD`.
- `last_updated_at`: required ISO-8601 UTC.

### API Required

Included in `POST /api/v1/sync` under `records.batches`:

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

### Success States

- New batch appears immediately in local inventory.
- Sync status shows Pending until the next successful sync.
- After sync HTTP 200, status becomes Synced.

### Error States

- Missing stock reference: block save locally.
- Duplicate stock reference in same pharmacy: block save locally where possible.
- HTTP 409 duplicate stock reference: keep row pending and show conflict.
- Missing product relationship: show that the product must sync before the batch.

### Edge Cases

- Two deliveries have the same manufacturer batch number: both must appear as separate rows.
- Legacy rows may have `STK-LEGACY-...` references. Display them as valid stock references.
- Very long package lot numbers should be blocked at 120 characters.
- User edits product after batches exist: keep batch `product_id` stable unless the user explicitly changes the selected product.

### Accessibility And Usability

- Place Stock Reference near Manufacturer Batch Number, but visually distinguish internal reference from package lot number.
- Provide helper text because these names are easy to confuse.
- Use table columns that can be scanned: Product, Stock Reference, Manufacturer Lot, Quantity, Expiry, Sync Status.

## Screen 5: Inventory And Product Management

### Purpose

Ensure product records sync with UUIDs and the current product field contract.

### Components To Add Or Modify

- Product name field.
- Description field.
- Category field.
- Reorder level field.
- Sync status badge per product row.
- Optional last updated timestamp in details panel.
- Hidden stable UUID per product record.

### Interaction And Behavior

- Creating a product generates a UUID locally.
- Editing a product marks it `MODIFIED`.
- Product records must sync before batches that reference them.
- Product IDs should never be regenerated after creation.

### Validation Rules

- `id`: required UUID string.
- `name`: required, max 255 characters.
- `description`: optional text.
- `category`: optional, max 120 characters.
- `reorder_level`: required integer, minimum 0.
- `sync_status`: `PENDING`, `MODIFIED`, or `SYNCED`.
- `last_updated_at`: required ISO-8601 UTC.

### API Required

Included in `POST /api/v1/sync` under `records.products`.

### Success States

- Product appears in product list immediately after local save.
- Product becomes Synced after HTTP 200.

### Error States

- Required name missing.
- Reorder level negative.
- Sync fails because a related batch references an invalid product.

### Edge Cases

- Product is created and batch is created before sync: both should be sent together, products first.
- Product has no category: send `null` or omit if local serializer allows it.
- Duplicate product names are not blocked by the server contract, so UI should not enforce global uniqueness unless product policy requires it.

### Accessibility And Usability

- Reorder level should use a numeric input with stepper support.
- Sync status should have text labels, not just icons.
- Avoid exposing UUIDs in normal user flows.

## Screen 6: Sales Checkout And Sales History

### Purpose

Ensure sales and sale items reference users and batches by UUID, not by names, dates, totals, or manufacturer batch numbers.

### Components To Add Or Modify

- Checkout batch selector should display enough information to disambiguate duplicate manufacturer lots:
  - Product name
  - Stock Reference
  - Manufacturer Batch Number
  - Expiry date
  - Available quantity
- Sales history should show sale date and total amount.
- Sale detail should show each sale item with product name, manufacturer batch number, quantity sold, unit price, and preferably stock reference if available locally.
- Sync status badge for sales and sale items.

### Interaction And Behavior

- Creating a sale generates a UUID locally for the sale.
- Creating each sale line generates a UUID locally for the sale item.
- Sale item must store `sale_id` and `batch_id` UUIDs.
- Do not use `batch_number` as the identifier for a sale line.
- Editing or voiding a sale should mark related records `MODIFIED` according to local business rules.

### Validation Rules

Sale:

- `id`: required UUID string.
- `user_id`: required UUID string.
- `sale_date`: required ISO-8601 UTC.
- `total_amount`: required decimal, minimum 0.00, max 12 integer digits and 2 fractional digits.
- `sync_status`: required.
- `last_updated_at`: required ISO-8601 UTC.

Sale item:

- `id`: required UUID string.
- `sale_id`: required UUID string.
- `batch_id`: required UUID string.
- `product_name`: required, max 255 characters.
- `batch_number`: required, max 120 characters.
- `quantity_sold`: required integer, minimum 0.
- `unit_price`: required decimal, minimum 0.00, max 12 integer digits and 2 fractional digits.
- `sync_status`: required.
- `last_updated_at`: required ISO-8601 UTC.

### API Required

Included in `POST /api/v1/sync` under:

- `records.sales`
- `records.sale_items`

### Success States

- Sale appears in local sales history immediately.
- Submitted sale and sale items become Synced after HTTP 200.

### Error States

- Missing user: block sale completion or create/select user first.
- Missing batch: block sale line creation.
- Sync fails with missing relationship: keep sale and dependent sale items pending.
- Network failure: sale remains usable locally and pending for retry.

### Edge Cases

- Duplicate manufacturer lot numbers: batch selector must show stock reference so user can choose the right stock delivery.
- Sale created while offline: keep it pending and sync later.
- User record and sale are both new: send users before sales in the same sync payload.
- Batch record and sale item are both new: send batches before sale items in the same sync payload.

### Accessibility And Usability

- Batch selector should be searchable by product, stock reference, and manufacturer batch number.
- Make stock reference copyable in detail views for support calls.
- Use clear empty states for no available batches.

## Screen 7: User Management

### Purpose

Keep user records compatible with server sync and avoid exposing sensitive password hash values in the UI.

### Components To Add Or Modify

- Username field.
- Role selector or field.
- Password creation/reset flow that stores a hash locally according to existing desktop rules.
- Sync status badge per user row.
- Last updated timestamp in details if appropriate.
- Do not display `password_hash` in ordinary UI.

### Interaction And Behavior

- Creating a user generates a UUID locally.
- Updating username, role, or password marks the user `MODIFIED`.
- User records must sync before sales that reference them.
- Do not log full user sync payloads because `password_hash` is included.

### Validation Rules

- `id`: required UUID string.
- `username`: required, max 100 characters.
- `password_hash`: required, max 255 characters.
- `role`: required, max 50 characters.
- `created_at`: required ISO-8601 UTC.
- `sync_status`: required.
- `last_updated_at`: required ISO-8601 UTC.

### API Required

Included in `POST /api/v1/sync` under `records.users`.

### Success States

- User appears locally after save.
- User becomes Synced after HTTP 200.

### Error States

- Missing username.
- Missing role.
- Password hash missing due to a failed local password flow.
- Sync failure due to sale referencing a missing user.

### Edge Cases

- User is created offline and immediately makes a sale: sync must include user before sale.
- Role names longer than 50 characters should be blocked.
- Existing legacy users may all sync again after migration.

### Accessibility And Usability

- Role selector should be keyboard accessible.
- Password reset feedback should not reveal password hashes.
- Sync errors involving users should avoid exposing password hash values.

## Screen 8: App Settings

### Purpose

Sync configurable settings such as receipt footer or local preference records.

### Components To Add Or Modify

- Settings list or form should preserve a stable UUID for each setting row.
- Setting key field.
- Setting value field.
- Sync status badge.
- Last updated timestamp if useful.

### Interaction And Behavior

- Creating a setting generates a UUID locally.
- Editing a setting marks it `MODIFIED`.
- Settings are processed after sales and sale items on the server.

### Validation Rules

- `id`: required UUID string.
- `setting_key`: required, max 150 characters.
- `setting_value`: optional text.
- `sync_status`: required.
- `last_updated_at`: required ISO-8601 UTC.

### API Required

Included in `POST /api/v1/sync` under `records.app_settings`.

### Success States

- Setting change is applied locally immediately.
- Setting becomes Synced after HTTP 200.

### Error States

- Missing setting key.
- Setting key too long.
- Network or server failure leaves setting pending.

### Edge Cases

- Empty setting value is valid if local business rules allow it.
- Local-only migration tables and legacy helper tables must not appear in the settings UI or sync payload.

### Accessibility And Usability

- Use clear save confirmation for settings that affect receipts or operational behavior.
- Keep sync status unobtrusive but visible in diagnostics.

## Screen 9: Admin Dashboard And Pharmacy Reports

### Purpose

Provide a user-friendly monitoring dashboard backed by the detailed records produced by desktop synchronization. A user must never type or search for a pharmacy UUID.

### Components To Add Or Modify

- Use `/api/v1/admin/...` as the only route prefix.
- Do not add compatibility fallbacks to unversioned `/api/...` routes.
- Show cards for pharmacy, inventory, stock-unit, sales, sync-success, sync-failure, and in-progress totals.
- Show the latest synchronization and a recent activity list with `SUCCESSFUL`, `FAILED`, or `IN_PROGRESS` badges.
- Load a pharmacy selector/list from the API and keep `pharmacy_id` only as internal route/query state.
- On selection, show pharmacy details, inventory statistics, sales statistics, and recent sync activity.
- Format `last_sync_at` locally as relative text such as ?Last synced 5 minutes ago,? while retaining an exact timestamp tooltip.

### API Required

Primary frontend endpoints:

```http
GET /api/v1/admin/dashboard
GET /api/v1/admin/pharmacies
GET /api/v1/admin/pharmacies/{pharmacyId}
GET /api/v1/admin/sync-activity?limit=20
GET /api/v1/admin/pharmacies/{pharmacyId}/sync-activity?limit=20
```

Optional detail-table and compatibility endpoints:

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/dashboard
GET /api/v1/admin/pharmacies/{pharmacyId}/inventory
GET /api/v1/admin/pharmacies/{pharmacyId}/sales
```

All current admin reports read `batches` and `sales` from the detailed sync model. No authentication header is required in the current local configuration. Browser access is allowed from the configured CORS origins, which default to localhost port `5173`.

### Success States

- Dashboard cards render totals after a successful response.
- The pharmacy list drives navigation without exposing identifier entry.
- Status messages use server-provided record counts, for example ?2 inventory records applied? or ?1 sale synchronized.?
- Report tables show rows or a clear empty state.

### Error States

- HTTP 404 for missing pharmacy.
- HTTP 400 for an activity `limit` outside `1..100`.
- HTTP 500 for server error.
- Network unavailable.

### Edge Cases

- Pharmacy was auto-created from sync and may have placeholder name/location.
- Admin searches by pharmacy should tolerate placeholder names until an admin edits them.
- A pharmacy may have `NEVER_SYNCED` and no `last_sync_at` when it predates activity auditing.
- A failed first sync may appear in global activity as `Unregistered pharmacy` without creating a selectable pharmacy row.
- Validation failures rejected before controller processing do not create activity rows.

### Accessibility And Usability

- Dashboard numbers should have text labels and not rely only on card position.
- Status badges must include readable text and not rely on color alone.
- Tables should support keyboard navigation and readable column headers.
- Empty states should distinguish no data from failed load.

## Cross-Screen Data Rules

### UUID Rules

All of these must be UUID strings:

- `users.id`
- `products.id`
- `batches.id`
- `sales.id`
- `sale_items.id`
- `app_settings.id`
- `batches.product_id`
- `sales.user_id`
- `sale_items.sale_id`
- `sale_items.batch_id`

Do not parse these as integers.

### Sync Status Rules

Allowed values:

```text
PENDING
MODIFIED
SYNCED
```

Recommended local behavior:

- New local row: `PENDING`
- Edited synced row: `MODIFIED`
- Row included in HTTP 200 sync response: `SYNCED`
- Failed sync: keep previous pending or modified state

### Date And Number Rules

- `expiry_date`: `YYYY-MM-DD`
- Timestamps: ISO-8601 UTC, for example `2026-07-20T12:00:00Z`
- Quantities and reorder levels: integers, minimum 0
- Money: decimal number, minimum 0.00, max 12 integer digits and 2 fractional digits

### Dependency Order

The frontend should build payloads in this dependency order:

1. `users`
2. `products`
3. `batches`
4. `sales`
5. `sale_items`
6. `app_settings`

The backend also processes in this order, but the payload should still be organized consistently for easier debugging.

### Records That Must Not Sync

Do not include local-only or migration helper tables:

- `local_schema_migrations`
- Any `*_legacy_*` tables

## Security And Production Notes

- The current `/api/v1/sync` endpoint accepts requests without an auth header so the current desktop client can sync.
- Do not expose this server publicly without HTTPS and an authentication plan.
- Do not log full sync payloads in frontend logs because `users.password_hash` is included.
- Show HTTPS warnings in setup screens for non-localhost URLs.
- Keep each pharmacy isolated by `pharmacyId`.

## Frontend Acceptance Checklist

- Backend base URL can be configured, stored locally, and health checked with `GET {baseUrl}/health`.
- Sync uses `/api/v1/sync` for new work.
- Sync sends top-level `pharmacyId` and nested `records`.
- Empty record groups are omitted or sent as empty arrays safely.
- No `X-Pharmacy-Token` is required for current `/api/v1/sync` calls.
- All synced IDs and foreign keys are UUID strings.
- Batch UI has both Stock Reference and Manufacturer Batch Number.
- Manufacturer batch numbers can repeat.
- Stock references are unique per pharmacy.
- Product, user, batch, sale, sale item, and app setting edits update local sync status.
- HTTP 200 marks submitted records `SYNCED`.
- Any non-200 leaves submitted records pending or modified.
- First sync after migration warns about possible historical re-upload.
- Sync errors are visible without blocking normal offline work.
- UI does not expose or log password hashes.
