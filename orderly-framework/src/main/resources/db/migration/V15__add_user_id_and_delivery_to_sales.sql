-- Adiciona a coluna user_id referenciando a tabela users.
-- Deixamos como NULL permitindo a migração mesmo que existam vendas antigas sem usuário
ALTER TABLE sales
    ADD COLUMN user_id UUID REFERENCES users(id);

-- Opcional: Adicionar colunas de endereço do pedido (snapshot) para preparar para o checkout
ALTER TABLE sales
    ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS delivery_street VARCHAR(255),
    ADD COLUMN IF NOT EXISTS delivery_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS delivery_neighborhood VARCHAR(255),
    ADD COLUMN IF NOT EXISTS delivery_city VARCHAR(255),
    ADD COLUMN IF NOT EXISTS delivery_zip_code VARCHAR(20);

CREATE INDEX idx_sales_user_id ON sales(user_id);
