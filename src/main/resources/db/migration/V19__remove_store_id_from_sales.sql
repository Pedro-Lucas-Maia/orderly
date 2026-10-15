-- 1. Remover índice que dependia de store_id
DROP INDEX IF EXISTS idx_sales_store_status;

-- 2. Remover coluna store_id da tabela sales
ALTER TABLE sales
    DROP COLUMN IF EXISTS store_id;

-- 3. Criar índice em status para otimizar filtros e contagem de pedidos em andamento
CREATE INDEX IF NOT EXISTS idx_sales_status ON sales(status);
