# Frontend Implementation Prompt: Pharmacy Monitoring Dashboard

Build a polished, responsive pharmacy monitoring dashboard against the API contract below. Treat this prompt as authoritative. Do not invent endpoints or fields.

## Product Goal

Create a user-friendly interface where an ordinary user can:

1. Open the dashboard and see system-wide pharmacy, inventory, sales, and synchronization totals.
2. Understand the latest sync as Successful, Failed, or In Progress.
3. Browse all registered pharmacies without typing, searching for, or understanding UUIDs.
4. Select a pharmacy and see its inventory, sales, current sync status, and recent activity.
5. Open inventory and sales tables when detailed records are needed.

UUIDs are internal identifiers. Store a selected `pharmacy_id` in application/router state and use it in API paths, but never render a UUID as a required user input.

## API Basics

- Local origin: `http://localhost:8080`
- All application routes use `/api/v1`; never fall back to an unversioned `/api/...` path.
- JSON fields are snake_case except the top-level sync request property `pharmacyId`.
- Timestamps are ISO-8601 UTC strings.
- Money is a JSON number; format it as currency in the UI without converting through binary floating-point for business calculations.
- No `Authorization`, cookie, API key, or `X-Pharmacy-Token` is currently required.
- Do not enable credentialed browser requests.
- The backend permits CORS from configured origins, defaulting to `http://localhost:5173` and `http://127.0.0.1:5173`.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Frontend Types

Use equivalent types in the chosen frontend language:

```ts
type SyncActivityStatus =
  | "NEVER_SYNCED"
  | "IN_PROGRESS"
  | "SUCCESSFUL"
  | "FAILED";

interface SyncActivity {
  id: string;
  pharmacy_id: string;
  pharmacy_name: string;
  status: Exclude<SyncActivityStatus, "NEVER_SYNCED">;
  started_at: string;
  completed_at?: string;
  duration_ms?: number;
  records_received: number;
  records_inserted: number;
  records_updated: number;
  records_ignored: number;
  inventory_records_received: number;
  inventory_records_applied: number;
  sales_records_received: number;
  sales_records_applied: number;
  message: string;
}

interface Dashboard {
  generated_at: string;
  total_pharmacies: number;
  pharmacies_with_successful_sync: number;
  total_inventory_records: number;
  total_units_in_stock: number;
  total_inventory_value: number;
  total_sales_count: number;
  total_sales_amount: number;
  successful_syncs: number;
  failed_syncs: number;
  in_progress_syncs: number;
  latest_sync?: SyncActivity;
  recent_activity: SyncActivity[];
}

interface PharmacySummary {
  pharmacy_id: string;
  pharmacy_name: string;
  location: string;
  sync_status: SyncActivityStatus;
  last_sync_at?: string;
  total_inventory_records: number;
  total_units_in_stock: number;
  total_inventory_value: number;
  total_sales_count: number;
  total_sales_amount: number;
}

interface PharmacyDetails {
  pharmacy_id: string;
  pharmacy_name: string;
  location: string;
  sync_status: SyncActivityStatus;
  last_sync_at?: string;
  inventory: {
    total_records: number;
    total_units_in_stock: number;
    total_value: number;
  };
  sales: {
    total_transactions: number;
    total_amount: number;
  };
  successful_syncs: number;
  failed_syncs: number;
  latest_sync?: SyncActivity;
  recent_activity: SyncActivity[];
}

interface InventoryRecord {
  id: string;
  pharmacy_id: string;
  product_id: string;
  product_name: string;
  category?: string;
  stock_reference: string;
  batch_number: string;
  quantity: number;
  cost_price: number;
  selling_price: number;
  inventory_value: number;
  expiry_date?: string;
  last_updated_at: string;
}

interface SaleRecord {
  id: string;
  pharmacy_id: string;
  user_id: string;
  username: string;
  total_amount: number;
  sale_date: string;
  item_count: number;
  last_updated_at: string;
}

interface ApiViolation {
  field: string;
  message: string;
}

interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  violations?: ApiViolation[];
}
```

The backend omits null fields, so optional timestamps and `latest_sync` may be absent rather than explicitly `null`.

## Endpoints To Integrate

### Health

```http
GET /api/v1/health
```

Response:

```json
{ "status": "UP" }
```

Use this for an optional connectivity indicator or retry action. Do not poll it aggressively.

### Global Dashboard

```http
GET /api/v1/admin/dashboard
```

No parameters or request body. Returns `Dashboard`. The response already combines totals and the ten latest sync attempts; do not reconstruct these cards with many unrelated requests.

On the landing page, show:

- Total pharmacies.
- Pharmacies with at least one successful audited sync.
- Inventory batch records.
- Units in stock.
- Inventory value.
- Sales transactions and total sales amount.
- Successful, failed, and in-progress attempt counts.
- Latest synchronization card.
- Recent activity list.

### Pharmacy List

```http
GET /api/v1/admin/pharmacies
```

No parameters or request body. Returns `PharmacySummary[]`, ordered by name.

Render searchable/selectable cards or rows with name, location, status, relative last-sync time, inventory units/value, and sales count/amount. Use `pharmacy_id` only as the internal key and navigation parameter.

### Pharmacy Details

```http
GET /api/v1/admin/pharmacies/{pharmacyId}
```

Required path parameter: the `pharmacy_id` from a selected `PharmacySummary`.

Returns `PharmacyDetails`. This is the main selected-pharmacy request and includes the latest and ten recent sync attempts. Provide a visible Back to pharmacies action.

### Global Activity

```http
GET /api/v1/admin/sync-activity?limit=20
```

Optional integer query parameter `limit`; default `20`, minimum `1`, maximum `100`. Returns `SyncActivity[]` newest first.

Use this when the user opens a dedicated all-activity view or requests more than the dashboard’s ten entries.

### Pharmacy Activity

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/sync-activity?limit=20
```

Required internal path parameter `pharmacyId`; optional `limit` with the same `1..100` rules. Returns `SyncActivity[]` newest first.

### Pharmacy Inventory

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/inventory
```

Returns `InventoryRecord[]`, ordered by product name and stock reference. Use it for a detailed inventory table, not for the summary cards already present in `PharmacyDetails`.

Show product, category, stock reference, manufacturer batch number, quantity, selling price, inventory value, expiry date, and last updated time. Manufacturer batch numbers may repeat; `stock_reference` identifies a stock delivery.

### Pharmacy Sales

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/sales
```

Returns `SaleRecord[]` newest first. Show sale time, cashier username, item count, total amount, and last update time.

### Compact Compatibility Dashboard

```http
GET /api/v1/admin/pharmacies/{pharmacyId}/dashboard
```

This existing endpoint remains supported, but a new frontend should prefer `GET /api/v1/admin/pharmacies/{pharmacyId}` because the comprehensive response includes nested statistics and recent activity.

Its response is:

```ts
interface PharmacyDashboard {
  pharmacy_id: string;
  pharmacy_name: string;
  location: string;
  sync_status: SyncActivityStatus;
  last_sync_at?: string;
  total_inventory_records: number;
  total_units_in_stock: number;
  total_inventory_value: number;
  total_sales_count: number;
  total_sales_amount: number;
  successful_syncs: number;
  failed_syncs: number;
}
```

## Desktop Sync Endpoint Context

The monitoring frontend should not normally call this endpoint. It is included so the UI contract stays consistent with the actual backend and so an authorized diagnostic screen does not invent a different payload.

```http
POST /api/v1/sync
Content-Type: application/json
```

Request:

```ts
interface SyncRequest {
  pharmacyId: string;
  records?: {
    users?: Array<{
      id: string;
      username: string;
      password_hash: string;
      role: string;
      created_at: string;
      sync_status: "PENDING" | "MODIFIED" | "SYNCED";
      last_updated_at: string;
    }>;
    products?: Array<{
      id: string;
      name: string;
      description?: string;
      category?: string;
      reorder_level: number;
      created_at: string;
      sync_status: "PENDING" | "MODIFIED" | "SYNCED";
      last_updated_at: string;
    }>;
    batches?: Array<{
      id: string;
      product_id: string;
      stock_reference: string;
      batch_number: string;
      quantity: number;
      cost_price: number;
      selling_price: number;
      expiry_date?: string;
      sync_status: "PENDING" | "MODIFIED" | "SYNCED";
      last_updated_at: string;
    }>;
    sales?: Array<{
      id: string;
      user_id: string;
      sale_date: string;
      total_amount: number;
      sync_status: "PENDING" | "MODIFIED" | "SYNCED";
      last_updated_at: string;
    }>;
    sale_items?: Array<{
      id: string;
      sale_id: string;
      batch_id: string;
      product_name: string;
      batch_number: string;
      quantity_sold: number;
      unit_price: number;
      sync_status: "PENDING" | "MODIFIED" | "SYNCED";
      last_updated_at: string;
    }>;
    app_settings?: Array<{
      id: string;
      setting_key: string;
      setting_value?: string;
      sync_status: "PENDING" | "MODIFIED" | "SYNCED";
      last_updated_at: string;
    }>;
  };
}
```

Missing groups are treated as empty. Unknown fields are ignored. The server processes groups in dependency order: users, products, batches, sales, sale items, app settings.

Success is exactly HTTP `200` after the complete transaction commits:

```json
{
  "pharmacy_id": "10000000-0000-0000-0000-000000000001",
  "users": { "inserted": 1, "updated": 0, "ignored": 0 },
  "products": { "inserted": 1, "updated": 0, "ignored": 0 },
  "batches": { "inserted": 1, "updated": 0, "ignored": 0 },
  "sales": { "inserted": 1, "updated": 0, "ignored": 0 },
  "sale_items": { "inserted": 1, "updated": 0, "ignored": 0 },
  "app_settings": { "inserted": 1, "updated": 0, "ignored": 0 }
}
```

Never log a full sync request because it currently contains `password_hash`. Never treat a record-level count as authorization to mark local rows synced; desktop clients use only the whole-request HTTP `200`.

## Status Presentation

Map backend values to readable labels:

- `NEVER_SYNCED` → “Never synced” with a neutral icon.
- `IN_PROGRESS` → “In progress” with an accessible progress indicator.
- `SUCCESSFUL` → “Successful” with a success icon.
- `FAILED` → “Failed” with an error icon and the server `message`.

Do not rely on color alone. Add visible text and accessible labels.

Format `last_sync_at`, `started_at`, and `completed_at` relative to the viewer’s current time, such as “5 minutes ago,” and provide the exact localized date/time in a tooltip or secondary line. Compute relative text in the frontend so it remains current and localized.

Use the activity counts to produce sentences such as:

- “125 inventory records received; 120 applied.”
- “48 sales transactions synchronized.”
- “6 records inserted, 2 updated, 3 already current.”

Do not call ignored records failures. They are equal/older retry records that the idempotent server intentionally did not overwrite.

## Navigation And Data Loading

- Route `/` or `/dashboard` to the global dashboard.
- Route `/pharmacies` to the pharmacy list.
- Route `/pharmacies/:pharmacyId` to details, but populate this route only through pharmacy-list selection.
- Optionally nest `/inventory`, `/sales`, and `/activity` under the selected pharmacy route.
- Fetch dashboard and pharmacy-list data in parallel on initial load if both are visible.
- Fetch `PharmacyDetails` after selection. Load inventory/sales arrays only when their tabs or sections are opened.
- Preserve the selected pharmacy during refresh using the route parameter, then validate it by calling pharmacy details.
- Never provide a text field labeled Pharmacy UUID or require UUID knowledge.

## Loading, Empty, And Error States

- Use skeletons for dashboard cards, pharmacy rows, and detail summaries.
- Disable duplicate refresh actions while the same request is pending.
- Show an empty dashboard message when all totals are zero and activity is empty.
- Show “No pharmacies have synchronized yet” for an empty pharmacy list.
- Show “No sync activity yet” when an activity array is empty.
- Show “No inventory records” and “No sales recorded” for empty detail tables.
- A missing `latest_sync` or `last_sync_at` is a valid never-synced state, not a parsing error.
- On HTTP `400`, display the shared `message` and useful `violations`; activity-limit controls must stay within `1..100`.
- On HTTP `404`, explain that the pharmacy no longer exists and return to the pharmacy list.
- On HTTP `409`, show the server conflict message. This normally applies to sync writes.
- On HTTP `500` or network failure, keep the last successfully rendered data visible when possible and offer Retry.
- Never display stack traces or raw HTML errors.

## Important Backend Semantics

- Dashboard and pharmacy statistics come from detailed synchronized `batches` and `sales`, not the legacy central reporting tables.
- Inventory records are batch rows; `total_units_in_stock` is the sum of batch quantities.
- Inventory value is quantity multiplied by selling price.
- Sales count is the number of sale headers, not sale items.
- Sync audit status is persisted separately from the operational transaction.
- `SUCCESSFUL` is set only after the full operational transaction commits.
- `FAILED` means processing failed and operational changes rolled back.
- Audit writes are best-effort so an audit problem cannot turn a committed desktop sync into a false failure.
- Request validation failures rejected before sync-controller processing return HTTP `400` but do not create activity rows.
- A failed first attempt can appear in global activity as `Unregistered pharmacy`; it will not appear in the selectable pharmacy list because placeholder pharmacy creation rolled back.
- Placeholder names and `Unknown` locations are valid until pharmacy-profile functionality is added.

## Acceptance Criteria

- A normal user opens the app and immediately sees system totals and the latest synchronization.
- The user browses and selects a pharmacy without entering an identifier.
- The selected pharmacy page explains inventory, sales, sync status, last-sync time, and recent activity.
- Successful, failed, in-progress, and never-synced states are visually and textually distinct.
- Loading, empty, 400, 404, 409, 500, and network-error states are handled.
- All requests use only documented `/api/v1` routes.
- No authentication header is sent in the current local setup.
- No sync request or password hash is logged.
- The UI does not invent fields or endpoints beyond this contract.
