CREATE TABLE sales (
    id VARCHAR(36) PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_value NUMERIC(12, 2) NOT NULL,
    observation TEXT
);

CREATE TABLE sale_items (
    id VARCHAR(36) PRIMARY KEY,
    sale_id VARCHAR(36) NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0)
);

CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
