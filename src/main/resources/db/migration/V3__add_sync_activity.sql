create table sync_activities (
    id uuid primary key,
    pharmacy_id uuid not null,
    status varchar(20) not null,
    started_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    records_received integer not null default 0,
    records_inserted integer not null default 0,
    records_updated integer not null default 0,
    records_ignored integer not null default 0,
    inventory_records_received integer not null default 0,
    inventory_records_applied integer not null default 0,
    sales_records_received integer not null default 0,
    sales_records_applied integer not null default 0,
    message varchar(1000) not null
);

create index idx_sync_activities_pharmacy_started
    on sync_activities (pharmacy_id, started_at desc);

create index idx_sync_activities_status
    on sync_activities (status);

create index idx_sync_activities_started_at
    on sync_activities (started_at desc);
