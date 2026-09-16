ALTER TABLE products
    ADD COLUMN deleted_at TIMESTAMP NULL;

ALTER TABLE stock_items
    ADD COLUMN deleted_at TIMESTAMP NULL;
