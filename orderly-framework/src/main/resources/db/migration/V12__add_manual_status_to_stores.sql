ALTER TABLE stores
    ADD COLUMN manual_status VARCHAR(20)
        CHECK (manual_status IN ('ABERTA', 'FECHADA', 'PAUSADA'));
