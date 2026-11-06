-- Lotes com validade (FEFO). Invariante: stock_items.current_quantity = soma das quantidades dos lotes.
-- Saldo inicial e entradas passam pela aplicação (lotes criados em create/ENTRADA/addStock).

CREATE TABLE stock_lots (
    id BIGSERIAL PRIMARY KEY,
    stock_item_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    expires_at DATE NOT NULL,
    received_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_stock_lots_stock_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items(id)
);

CREATE INDEX idx_stock_lots_item_expiry
    ON stock_lots (stock_item_id, expires_at, id);

ALTER TABLE stock_movements
    ADD COLUMN lot_id BIGINT,
    ADD CONSTRAINT fk_stock_movements_lot
        FOREIGN KEY (lot_id)
        REFERENCES stock_lots(id)
        ON DELETE SET NULL;
