CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE stock_items (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    current_quantity INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 0,
    unit_cost NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_stock_items_stock
        FOREIGN KEY (stock_id)
        REFERENCES stocks(id)
);

CREATE TABLE product_compositions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    stock_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_product_compositions_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT fk_product_compositions_stock_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items(id),
    CONSTRAINT uq_product_compositions_product_stock_item
        UNIQUE (product_id, stock_item_id)
);

CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    stock_item_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    reason VARCHAR(255) NOT NULL,
    CONSTRAINT fk_stock_movements_stock_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items(id)
);
