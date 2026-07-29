alter table batches add column stock_reference varchar(64);

update batches
set stock_reference = 'STK-LEGACY-' || replace(cast(id as varchar), '-', '')
where stock_reference is null;

alter table batches alter column stock_reference set not null;

create unique index ux_batches_pharmacy_stock_reference on batches (pharmacy_id, stock_reference);
