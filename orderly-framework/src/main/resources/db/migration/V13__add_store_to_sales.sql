ALTER TABLE sales
    ADD COLUMN store_id BIGINT NOT NULL REFERENCES stores(id);

CREATE INDEX idx_sales_store_status ON sales(store_id, status);
