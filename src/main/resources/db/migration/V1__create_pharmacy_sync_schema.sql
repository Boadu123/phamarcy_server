create table pharmacies (
    id uuid primary key,
    name varchar(255) not null,
    location varchar(500) not null,
    api_token varchar(255) not null
);

create unique index ux_pharmacies_api_token on pharmacies (api_token);

create table central_inventory (
    id uuid primary key,
    pharmacy_id uuid not null,
    product_name varchar(255) not null,
    quantity integer not null,
    price numeric(14, 2) not null,
    last_updated_at timestamp with time zone not null,
    constraint fk_central_inventory_pharmacy
        foreign key (pharmacy_id) references pharmacies (id)
);

create index idx_central_inventory_pharmacy_id on central_inventory (pharmacy_id);
create index idx_central_inventory_last_updated_at on central_inventory (last_updated_at);

create table central_sales (
    id uuid primary key,
    pharmacy_id uuid not null,
    total_amount numeric(14, 2) not null,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    constraint fk_central_sales_pharmacy
        foreign key (pharmacy_id) references pharmacies (id)
);

create index idx_central_sales_pharmacy_id on central_sales (pharmacy_id);
create index idx_central_sales_created_at on central_sales (created_at);
create index idx_central_sales_last_updated_at on central_sales (last_updated_at);

create table users (
    id uuid primary key,
    pharmacy_id uuid not null,
    username varchar(100) not null,
    password_hash varchar(255) not null,
    role varchar(50) not null,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    deleted boolean not null default false,
    constraint fk_users_pharmacy
        foreign key (pharmacy_id) references pharmacies (id)
);

create unique index ux_users_pharmacy_username on users (pharmacy_id, username);
create index idx_users_pharmacy_id on users (pharmacy_id);
create index idx_users_last_updated_at on users (last_updated_at);

create table products (
    id uuid primary key,
    pharmacy_id uuid not null,
    name varchar(255) not null,
    description text,
    category varchar(120),
    reorder_level integer not null default 0,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    deleted boolean not null default false,
    constraint fk_products_pharmacy
        foreign key (pharmacy_id) references pharmacies (id)
);

create index idx_products_pharmacy_id on products (pharmacy_id);
create index idx_products_last_updated_at on products (last_updated_at);

create table batches (
    id uuid primary key,
    pharmacy_id uuid not null,
    product_id uuid not null,
    batch_number varchar(120) not null,
    quantity integer not null,
    cost_price numeric(14, 2) not null,
    selling_price numeric(14, 2) not null,
    expiry_date date,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    deleted boolean not null default false,
    constraint fk_batches_pharmacy
        foreign key (pharmacy_id) references pharmacies (id),
    constraint fk_batches_product
        foreign key (product_id) references products (id)
);

create index idx_batches_pharmacy_id on batches (pharmacy_id);
create index idx_batches_product_id on batches (product_id);
create index idx_batches_expiry_date on batches (expiry_date);
create index idx_batches_last_updated_at on batches (last_updated_at);

create table sales (
    id uuid primary key,
    pharmacy_id uuid not null,
    user_id uuid not null,
    sale_date timestamp with time zone not null,
    total_amount numeric(14, 2) not null,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    deleted boolean not null default false,
    constraint fk_sales_pharmacy
        foreign key (pharmacy_id) references pharmacies (id),
    constraint fk_sales_user
        foreign key (user_id) references users (id)
);

create index idx_sales_pharmacy_id on sales (pharmacy_id);
create index idx_sales_user_id on sales (user_id);
create index idx_sales_sale_date on sales (sale_date);
create index idx_sales_created_at on sales (created_at);
create index idx_sales_last_updated_at on sales (last_updated_at);

create table sale_items (
    id uuid primary key,
    pharmacy_id uuid not null,
    sale_id uuid not null,
    batch_id uuid not null,
    product_name varchar(255) not null,
    batch_number varchar(120) not null,
    quantity_sold integer not null,
    unit_price numeric(14, 2) not null,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    deleted boolean not null default false,
    constraint fk_sale_items_pharmacy
        foreign key (pharmacy_id) references pharmacies (id),
    constraint fk_sale_items_sale
        foreign key (sale_id) references sales (id),
    constraint fk_sale_items_batch
        foreign key (batch_id) references batches (id)
);

create index idx_sale_items_pharmacy_id on sale_items (pharmacy_id);
create index idx_sale_items_sale_id on sale_items (sale_id);
create index idx_sale_items_batch_id on sale_items (batch_id);
create index idx_sale_items_last_updated_at on sale_items (last_updated_at);

create table app_settings (
    id uuid primary key,
    pharmacy_id uuid not null,
    setting_key varchar(150) not null,
    setting_value text,
    created_at timestamp with time zone not null,
    last_updated_at timestamp with time zone not null,
    deleted boolean not null default false,
    constraint fk_app_settings_pharmacy
        foreign key (pharmacy_id) references pharmacies (id)
);

create unique index ux_app_settings_pharmacy_key on app_settings (pharmacy_id, setting_key);
create index idx_app_settings_pharmacy_id on app_settings (pharmacy_id);
create index idx_app_settings_last_updated_at on app_settings (last_updated_at);