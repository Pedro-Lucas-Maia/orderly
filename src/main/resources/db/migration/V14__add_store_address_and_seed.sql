-- 1. Adicionar colunas de endereço na tabela stores
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS address_street VARCHAR(255),
ADD COLUMN IF NOT EXISTS address_number VARCHAR(50),
ADD COLUMN IF NOT EXISTS address_neighborhood VARCHAR(255),
ADD COLUMN IF NOT EXISTS address_city VARCHAR(255),
ADD COLUMN IF NOT EXISTS address_zip_code VARCHAR(20);

-- 2. Inserir ou atualizar a Loja Padrão (ID = 1) com o endereço
INSERT INTO stores (
    id, name, opening_time, closing_time, 
    max_orders_in_progress, automatic_pause, status, 
    address_street, address_number, address_neighborhood, address_city, address_zip_code
)
VALUES (
    1, 'Loja Principal', '08:00:00', '22:00:00', 
    50, false, 'ABERTA', 
    'Av. Senador Salgado Filho', '2234', 'Candelária', 'Natal', '59064-900'
)
ON CONFLICT (id) DO UPDATE SET
    address_street = EXCLUDED.address_street,
    address_number = EXCLUDED.address_number,
    address_neighborhood = EXCLUDED.address_neighborhood,
    address_city = EXCLUDED.address_city,
    address_zip_code = EXCLUDED.address_zip_code;

-- 3. Inserir Usuário Admin (aproveitando o ROLE_ADMIN já existente da V1)
INSERT INTO users (
    id, name, cpf, email, password, role_id, locked, enabled, created_at, updated_at, failed_login_attempts
)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 
    'Administrador', 
    '00000000000', 
    'admin@orderly.com', 
    '$2a$10$XgG.f.K8D2m8/fH1v3M/.uTqYf.rZ/tO2sL5.s9bO/vR.X8cR6j', -- senha: admin123
    'b30349f5-1049-43c2-841f-1393663a75f1', -- ID do ROLE_ADMIN inserido na V1__create_tables.sql
    false, 
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (email) DO NOTHING;
