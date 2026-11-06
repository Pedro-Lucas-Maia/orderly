-- Seed para demonstração de relatórios gerenciais e insights preditivos.
-- NÃO é migration Flyway. Deve ser executado diretamente no banco (ex: psql).
-- Totalmente idempotente: pode ser reexecutado múltiplas vezes sem conflitos.
--
-- Cobertura do Seed:
-- 1. Usuários e Endereços: 10 clientes com UUIDs v4 válidos, senha no padrão {bcrypt} (senha: admin123) e endereços em Natal/RN.
-- 2. Vendas e Itens: ~3 meses (~90 dias) de histórico realista (~2.400 pedidos e ~7.900 itens):
--    - Curva semanal com pico em fins de semana (Sex/Sáb/Dom) e menor fluxo às segundas.
--    - Status ENTREGUE (95%) e CANCELADA (5%) para histórico; status PENDENTE, EM_PREPARO, EM_ROTA e ENTREGUE para o dia atual.
--    - Endereço de entrega snapshot e taxa de frete calculada por bairro (Capim Macio, Ponta Negra, Tirol, Lagoa Nova, etc.).
-- 3. Movimentações de Estoque (~3.200 movimentações cobrindo os 4 tipos):
--    - ENTRADA: 13 entregas semanais periódicas de fornecedores para todos os insumos.
--    - SAÍDA: Consumo diário operacional derivado das composições (product_compositions) dos pedidos entregues.
--    - PERDA: Perdas operacionais em perecíveis (folhas murchas, validade de abertos, avarias de transporte).
--    - AJUSTE: Ajustes mensais de inventário físico das contagens periódicas.
-- 4. Saldo Atual e Lotes de Estoque (FEFO):
--    - Invariante mantida rigorosamente: stock_items.current_quantity = SUM(stock_lots.quantity).
--    - Insumos críticos abaixo do estoque mínimo (Pão Brioche, Queijo Prato, Bacon, Sorvete Morango, Alface).
--    - Lotes próximos ao vencimento (1 a 3 dias) para acionar o motor preditivo de promoções e sugestões de compra.

BEGIN;

-- ============================================================================
-- 1. LIMPEZA DE DADOS ANTERIORES DA DEMO (Garantia de Idempotência)
-- ============================================================================

-- Apagar itens de vendas geradas pela demo
DELETE FROM sale_items 
WHERE sale_id IN (SELECT id FROM sales WHERE observation LIKE 'INSIGHTS_DEMO%');

-- Apagar vendas geradas pela demo
DELETE FROM sales 
WHERE observation LIKE 'INSIGHTS_DEMO%';

-- Apagar movimentações de estoque da demo
DELETE FROM stock_movements;

-- Apagar lotes de estoque
DELETE FROM stock_lots;

-- Apagar endereços dos usuários demo
DELETE FROM user_addresses 
WHERE user_id IN (
    'a0000001-0000-4000-8000-000000000001',
    'a0000002-0000-4000-8000-000000000002',
    'a0000003-0000-4000-8000-000000000003',
    'a0000004-0000-4000-8000-000000000004',
    'a0000005-0000-4000-8000-000000000005',
    'a0000006-0000-4000-8000-000000000006',
    'a0000007-0000-4000-8000-000000000007',
    'a0000008-0000-4000-8000-000000000008',
    'a0000009-0000-4000-8000-000000000009',
    'a0000010-0000-4000-8000-000000000010'
);

-- Apagar usuários demo
DELETE FROM users 
WHERE id IN (
    'a0000001-0000-4000-8000-000000000001',
    'a0000002-0000-4000-8000-000000000002',
    'a0000003-0000-4000-8000-000000000003',
    'a0000004-0000-4000-8000-000000000004',
    'a0000005-0000-4000-8000-000000000005',
    'a0000006-0000-4000-8000-000000000006',
    'a0000007-0000-4000-8000-000000000007',
    'a0000008-0000-4000-8000-000000000008',
    'a0000009-0000-4000-8000-000000000009',
    'a0000010-0000-4000-8000-000000000010'
);

-- ============================================================================
-- 2. LOJA E USUÁRIO ADMINISTRADOR
-- ============================================================================

-- Garantir que a Loja 1 exista e esteja ABERTA
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
    status = 'ABERTA';

-- Garantir que o usuário Admin exista com {bcrypt}
INSERT INTO users (
    id, name, cpf, email, password, role_id, locked, enabled, created_at, updated_at, failed_login_attempts
)
VALUES (
    '1e86ee0b-3d68-4f55-bd35-e7781bd1f3eb',
    'Administrador', 
    '00000000000', 
    'admin@orderly.com', 
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.', -- senha: admin123
    'b30349f5-1049-43c2-841f-1393663a75f1', -- ROLE_ADMIN
    false, 
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    enabled = true,
    locked = false;

-- ============================================================================
-- 3. USUÁRIOS CLIENTES (UUID v4 válidos e senha {bcrypt} = admin123)
-- ============================================================================

INSERT INTO users (id, name, cpf, email, password, role_id, locked, enabled, created_at, updated_at, failed_login_attempts)
VALUES
(
    'a0000001-0000-4000-8000-000000000001',
    'Carlos Eduardo Silva',
    '11122233301',
    'carlos.silva@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6', -- ROLE_USER
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000002-0000-4000-8000-000000000002',
    'Mariana Souza Costa',
    '11122233302',
    'mariana.souza@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000003-0000-4000-8000-000000000003',
    'Lucas Ferreira Lima',
    '11122233303',
    'lucas.ferreira@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000004-0000-4000-8000-000000000004',
    'Beatriz Costa Oliveira',
    '11122233304',
    'beatriz.costa@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000005-0000-4000-8000-000000000005',
    'Felipe Albuquerque Melo',
    '11122233305',
    'felipe.albuquerque@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000006-0000-4000-8000-000000000006',
    'Juliana Lima Santos',
    '11122233306',
    'juliana.lima@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000007-0000-4000-8000-000000000007',
    'Rodrigo Santos Ribeiro',
    '11122233307',
    'rodrigo.santos@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000008-0000-4000-8000-000000000008',
    'Camila Martins Pereira',
    '11122233308',
    'camila.martins@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000009-0000-4000-8000-000000000009',
    'Gustavo Henrique Rocha',
    '11122233309',
    'gustavo.rocha@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
),
(
    'a0000010-0000-4000-8000-000000000010',
    'Larissa Mendes Barbosa',
    '11122233310',
    'larissa.mendes@demo.orderly.com',
    '{bcrypt}$2a$10$dPrFUhotGLpzmv1xXfE.KOqYeLOnJyu7BHrwz2.mI1ggBorABe1o.',
    '07279c16-2495-4670-8d5c-3725595304b6',
    false, true, CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '100 days', 0
);

-- Endereços dos Clientes Demo em Natal/RN
INSERT INTO user_addresses (id, user_id, name, street, number, complement, neighborhood, city, state, zip_code)
VALUES
('b0000001-0000-4000-8000-000000000001', 'a0000001-0000-4000-8000-000000000001', 'Casa', 'Av. Engenheiro Roberto Freire', '1500', 'Apto 302', 'Capim Macio', 'Natal', 'RN', '59082-000'),
('b0000002-0000-4000-8000-000000000002', 'a0000002-0000-4000-8000-000000000002', 'Casa', 'Rua Mossoró', '450', 'Bloco B, Ap 12', 'Tirol', 'Natal', 'RN', '59020-120'),
('b0000003-0000-4000-8000-000000000003', 'a0000003-0000-4000-8000-000000000003', 'Casa', 'Av. Prudente de Morais', '2100', 'Sala 4', 'Lagoa Nova', 'Natal', 'RN', '59063-200'),
('b0000004-0000-4000-8000-000000000004', 'a0000004-0000-4000-8000-000000000004', 'Casa', 'Rua Praia de Ponta Negra', '88', NULL, 'Ponta Negra', 'Natal', 'RN', '59090-100'),
('b0000005-0000-4000-8000-000000000005', 'a0000005-0000-4000-8000-000000000005', 'Trabalho', 'Av. Senador Salgado Filho', '3000', 'Escritório 10', 'Candelária', 'Natal', 'RN', '59064-740'),
('b0000006-0000-4000-8000-000000000006', 'a0000006-0000-4000-8000-000000000006', 'Casa', 'Rua Trairi', '512', NULL, 'Petrópolis', 'Natal', 'RN', '59014-150'),
('b0000007-0000-4000-8000-000000000007', 'a0000007-0000-4000-8000-000000000007', 'Casa', 'Av. Ayrton Senna', '1200', 'Casa 45', 'Neópolis', 'Natal', 'RN', '59080-100'),
('b0000008-0000-4000-8000-000000000008', 'a0000008-0000-4000-8000-000000000008', 'Casa', 'Rua Coronel Auris Coelho', '285', 'Apto 101', 'Nova Descoberta', 'Natal', 'RN', '59075-050'),
('b0000009-0000-4000-8000-000000000009', 'a0000009-0000-4000-8000-000000000009', 'Casa', 'Av. Hermes da Fonseca', '1050', NULL, 'Tirol', 'Natal', 'RN', '59020-000'),
('b0000010-0000-4000-8000-000000000010', 'a0000010-0000-4000-8000-000000000010', 'Casa', 'Rua Jaguarari', '1890', 'Condomínio Bela Vista', 'Lagoa Nova', 'Natal', 'RN', '59062-500');

-- ============================================================================
-- 4. VENDAS E ITENS (Histórico de 90 dias)
-- ============================================================================

-- Tabela temporária para arquétipos de pedidos (combos representativos do cardápio)
CREATE TEMP TABLE tmp_archetype_items (
    archetype_id INT,
    product_id BIGINT,
    quantity INT,
    unit_price NUMERIC(12, 2)
) ON COMMIT DROP;

INSERT INTO tmp_archetype_items (archetype_id, product_id, quantity, unit_price) VALUES
-- Combo 0: Classic Burger + Frita Tradicional + Coca-Cola
(0, 1, 1, 28.90), (0, 6, 1, 14.90), (0, 11, 1, 7.00),
-- Combo 1: Bacon Supreme + Batata Cheddar Bacon + Coca-Cola
(1, 2, 1, 34.90), (1, 7, 1, 24.90), (1, 11, 1, 7.00),
-- Combo 2: Double Smash + Batata Tradicional + Guaraná
(2, 3, 1, 32.90), (2, 6, 1, 14.90), (2, 12, 1, 7.00),
-- Combo 3: Crispy Chicken + Batata Tradicional + Shake Morango
(3, 4, 1, 29.90), (3, 6, 1, 14.90), (3, 8, 1, 22.90),
-- Combo 4: Spicy Chicken + Batata Cheddar Bacon + Sprite
(4, 5, 1, 31.90), (4, 7, 1, 24.90), (4, 13, 1, 7.00),
-- Combo 5 (Duplo / Casal): Classic Burger + Bacon Supreme + Batata Cheddar Bacon + 2 Coca-Cola
(5, 1, 1, 28.90), (5, 2, 1, 34.90), (5, 7, 1, 24.90), (5, 11, 2, 7.00),
-- Combo 6 (Sobremesa / Lanche leve): Batata Tradicional + Shake Chocolate + Shake Baunilha
(6, 6, 1, 14.90), (6, 9, 1, 24.90), (6, 10, 1, 21.90),
-- Combo 7: 2 Double Smash + 2 Batata Tradicional + 2 Guaraná
(7, 3, 2, 32.90), (7, 6, 2, 14.90), (7, 12, 2, 7.00),
-- Combo 8: Classic Burger + Shake Morango
(8, 1, 1, 28.90), (8, 8, 1, 22.90),
-- Combo 9 (Combo Família): 2 Classic Burger + Bacon Supreme + Double Smash + 2 Batata Cheddar Bacon + 3 Coca-Cola + Guaraná
(9, 1, 2, 28.90), (9, 2, 1, 34.90), (9, 3, 1, 32.90), (9, 7, 2, 24.90), (9, 11, 3, 7.00), (9, 12, 1, 7.00);

-- Mapeamento de usuários com endereços e taxas de entrega de Natal
CREATE TEMP TABLE tmp_user_addresses AS
SELECT u.id AS user_id, a.street, a.number, a.neighborhood, a.city, a.zip_code,
       CASE 
           WHEN a.neighborhood IN ('Ponta Negra', 'Capim Macio', 'Neópolis') THEN 5.00
           WHEN a.neighborhood IN ('Candelária', 'Lagoa Nova', 'Nova Descoberta', 'Tirol', 'Petrópolis') THEN 7.50
           ELSE 10.00
       END AS delivery_fee,
       ROW_NUMBER() OVER (ORDER BY u.id)::int AS user_num
FROM users u
JOIN user_addresses a ON a.user_id = u.id
WHERE u.id BETWEEN 'a0000001-0000-4000-8000-000000000001' AND 'a0000010-0000-4000-8000-000000000010';

-- Gerar pedidos diários por 90 dias com volume variável por dia da semana
CREATE TEMP TABLE tmp_generated_orders (
    order_id UUID PRIMARY KEY,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    user_id UUID NOT NULL,
    delivery_fee NUMERIC(12, 2) NOT NULL,
    delivery_street VARCHAR(255) NOT NULL,
    delivery_number VARCHAR(50) NOT NULL,
    delivery_neighborhood VARCHAR(255) NOT NULL,
    delivery_city VARCHAR(255) NOT NULL,
    delivery_zip_code VARCHAR(20) NOT NULL,
    archetype_id INT NOT NULL
) ON COMMIT DROP;

INSERT INTO tmp_generated_orders
SELECT 
    gen_random_uuid() AS order_id,
    (d.day_date + 
        CASE 
            WHEN s.order_seq <= (s.daily_orders / 3) 
                THEN (TIME '11:30:00' + ((s.order_seq * 13) % 150) * INTERVAL '1 minute')
            ELSE 
                (TIME '18:00:00' + ((s.order_seq * 17) % 240) * INTERVAL '1 minute')
        END
    ) AS order_date,
    CASE 
        WHEN d.days_ago > 0 THEN 
            CASE WHEN s.order_seq % 20 = 0 THEN 'CANCELADA' ELSE 'ENTREGUE' END
        ELSE 
            CASE 
                WHEN s.order_seq % 12 = 0 THEN 'CANCELADA'
                WHEN s.order_seq % 6 = 1 THEN 'PENDENTE'
                WHEN s.order_seq % 6 = 2 THEN 'EM_PREPARO'
                WHEN s.order_seq % 6 = 3 THEN 'EM_ROTA'
                ELSE 'ENTREGUE'
            END
    END AS status,
    ua.user_id,
    ua.delivery_fee,
    ua.street AS delivery_street,
    ua.number AS delivery_number,
    ua.neighborhood AS delivery_neighborhood,
    ua.city AS delivery_city,
    ua.zip_code AS delivery_zip_code,
    ((d.days_ago * 3 + s.order_seq) % 10)::int AS archetype_id
FROM (
    SELECT 
        (CURRENT_DATE - (days_ago || ' days')::interval)::date AS day_date,
        days_ago,
        EXTRACT(DOW FROM (CURRENT_DATE - (days_ago || ' days')::interval)::date)::int AS dow
    FROM generate_series(0, 90) AS days_ago
) d
CROSS JOIN LATERAL (
    SELECT 
        order_seq,
        CASE d.dow
            WHEN 0 THEN 32 -- Domingo (pico)
            WHEN 1 THEN 16 -- Segunda
            WHEN 2 THEN 18 -- Terça
            WHEN 3 THEN 20 -- Quarta
            WHEN 4 THEN 22 -- Quinta
            WHEN 5 THEN 36 -- Sexta (pico)
            WHEN 6 THEN 40 -- Sábado (pico)
        END AS daily_orders
    FROM generate_series(1, 
        CASE d.dow
            WHEN 0 THEN 32
            WHEN 1 THEN 16
            WHEN 2 THEN 18
            WHEN 3 THEN 20
            WHEN 4 THEN 22
            WHEN 5 THEN 36
            WHEN 6 THEN 40
        END
    ) AS order_seq
) s
JOIN tmp_user_addresses ua 
    ON ua.user_num = (((d.days_ago * 7 + s.order_seq) % 10) + 1);

-- Gerar itens para cada pedido gerado
CREATE TEMP TABLE tmp_generated_items (
    item_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL
) ON COMMIT DROP;

INSERT INTO tmp_generated_items
SELECT 
    gen_random_uuid() AS item_id,
    o.order_id,
    ai.product_id,
    ai.quantity,
    ai.unit_price,
    (ai.quantity * ai.unit_price) AS subtotal
FROM tmp_generated_orders o
JOIN tmp_archetype_items ai ON ai.archetype_id = o.archetype_id;

-- Inserir vendas consolidadas em sales
INSERT INTO sales (
    id, date, status, total_value, observation, user_id, 
    delivery_fee, delivery_street, delivery_number, delivery_neighborhood, delivery_city, delivery_zip_code
)
SELECT 
    o.order_id,
    o.order_date,
    o.status,
    (t.items_subtotal + o.delivery_fee) AS total_value,
    'INSIGHTS_DEMO',
    o.user_id,
    o.delivery_fee,
    o.delivery_street,
    o.delivery_number,
    o.delivery_neighborhood,
    o.delivery_city,
    o.delivery_zip_code
FROM tmp_generated_orders o
JOIN (
    SELECT order_id, SUM(subtotal) AS items_subtotal
    FROM tmp_generated_items
    GROUP BY order_id
) t ON t.order_id = o.order_id;

-- Inserir itens em sale_items
INSERT INTO sale_items (id, sale_id, product_id, quantity, unit_price, subtotal)
SELECT item_id, order_id, product_id, quantity, unit_price, subtotal
FROM tmp_generated_items;

-- ============================================================================
-- 5. MOVIMENTAÇÕES DE ESTOQUE (Histórico de 90 dias com os 4 tipos)
-- ============================================================================

-- 5.1 ENTRADA: Reabastecimento semanal de mercadoria (13 semanas para todos os 30 insumos)
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 
    si.id,
    'ENTRADA',
    CASE si.unit
        WHEN 'UNIDADE' THEN 
            CASE 
                WHEN si.id IN (1, 2, 3) THEN 250
                WHEN si.id IN (10, 11) THEN 200
                WHEN si.id IN (14, 16) THEN 120
                WHEN si.id IN (28, 29, 30) THEN 220
                ELSE 300
            END
        WHEN 'GRAMA' THEN
            CASE
                WHEN si.id = 18 THEN 35000 -- Batata Frita: 35kg
                WHEN si.id IN (4, 7, 8, 20, 24, 26) THEN 8000
                WHEN si.id IN (9, 12, 17, 19, 22) THEN 4000
                ELSE 2000
            END
        WHEN 'MILILITRO' THEN
            CASE
                WHEN si.id = 21 THEN 18000 -- Leite: 18L
                WHEN si.id IN (6, 13, 15, 23) THEN 4000
                ELSE 1000
            END
    END,
    w.delivery_date,
    'Recebimento semanal de fornecedor NF #' || (1000 + w.week_num)
FROM (
    SELECT 
        week_num,
        (CURRENT_DATE - INTERVAL '90 days' + (week_num * INTERVAL '7 days') + TIME '08:30:00')::timestamp AS delivery_date
    FROM generate_series(0, 12) AS week_num
) w
CROSS JOIN stock_items si;

-- 5.2 SAÍDA: Consumo diário operacional derivado diretamente das vendas entregues e das composições dos produtos
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 
    pc.stock_item_id,
    'SAIDA',
    SUM(gi.quantity * pc.quantity)::int,
    (go.order_date::date + TIME '22:45:00')::timestamp,
    'Consumo operacional diário de insumos'
FROM tmp_generated_orders go
JOIN tmp_generated_items gi ON gi.order_id = go.order_id
JOIN product_compositions pc ON pc.product_id = gi.product_id
WHERE go.status = 'ENTREGUE'
GROUP BY pc.stock_item_id, go.order_date::date;

-- 5.3 PERDA: Perdas operacionais periódicas (avarias, quebras e perecibilidade)
-- Alface americana (Insumo 4): descarte a cada 3 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 4, 'PERDA', 250, (CURRENT_DATE - (d || ' days')::interval + TIME '16:00:00')::timestamp, 'Descarte de hortifruti por perda de qualidade / murchamento'
FROM generate_series(1, 89, 3) AS d;

-- Tomate rodelas (Insumo 5): descarte a cada 4 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 5, 'PERDA', 15, (CURRENT_DATE - (d || ' days')::interval + TIME '16:00:00')::timestamp, 'Descarte de tomate avariado / passado'
FROM generate_series(2, 88, 4) AS d;

-- Salada Coleslaw (Insumo 17): descarte a cada 5 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 17, 'PERDA', 200, (CURRENT_DATE - (d || ' days')::interval + TIME '17:00:00')::timestamp, 'Validade expirada após abertura do pacote'
FROM generate_series(3, 87, 5) AS d;

-- Pão Brioche (Insumo 1): perda no manuseio a cada 6 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 1, 'PERDA', 6, (CURRENT_DATE - (d || ' days')::interval + TIME '14:00:00')::timestamp, 'Pães amassados na embalagem / avaria'
FROM generate_series(4, 86, 6) AS d;

-- Leite Integral (Insumo 21): leite aberto / descarte a cada 7 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 21, 'PERDA', 600, (CURRENT_DATE - (d || ' days')::interval + TIME '15:00:00')::timestamp, 'Descarte de leite aberto fora do padrão'
FROM generate_series(5, 85, 7) AS d;

-- Bacon em fatias (Insumo 8): descarte de aparas e queima a cada 8 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 8, 'PERDA', 180, (CURRENT_DATE - (d || ' days')::interval + TIME '18:00:00')::timestamp, 'Descarte de aparas e queima excessiva'
FROM generate_series(6, 84, 8) AS d;

-- Batata Frita Palito (Insumo 18): descarte no processo a cada 5 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 18, 'PERDA', 500, (CURRENT_DATE - (d || ' days')::interval + TIME '21:00:00')::timestamp, 'Descarte de batata queimada na fritadeira'
FROM generate_series(2, 88, 5) AS d;

-- Sorvete de Morango (Insumo 20): perda térmica a cada 10 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 20, 'PERDA', 300, (CURRENT_DATE - (d || ' days')::interval + TIME '20:00:00')::timestamp, 'Cristalização por oscilação térmica'
FROM generate_series(7, 85, 10) AS d;

-- Latas de Coca-Cola (Insumo 28): avarias de transporte a cada 14 dias
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
SELECT 28, 'PERDA', 2, (CURRENT_DATE - (d || ' days')::interval + TIME '10:00:00')::timestamp, 'Avaria de transporte / lata amassada'
FROM generate_series(10, 85, 14) AS d;

-- 5.4 AJUSTE: Auditorias de inventário físico mensal (60 dias atrás, 30 dias atrás e 5 dias atrás)
INSERT INTO stock_movements (stock_item_id, type, quantity, occurred_at, reason)
VALUES
(1, 'AJUSTE', 8, (CURRENT_DATE - INTERVAL '60 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(3, 'AJUSTE', 12, (CURRENT_DATE - INTERVAL '60 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(18, 'AJUSTE', 800, (CURRENT_DATE - INTERVAL '60 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(28, 'AJUSTE', 5, (CURRENT_DATE - INTERVAL '60 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),

(2, 'AJUSTE', 6, (CURRENT_DATE - INTERVAL '30 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(7, 'AJUSTE', 250, (CURRENT_DATE - INTERVAL '30 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(10, 'AJUSTE', 10, (CURRENT_DATE - INTERVAL '30 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(18, 'AJUSTE', 1200, (CURRENT_DATE - INTERVAL '30 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(21, 'AJUSTE', 500, (CURRENT_DATE - INTERVAL '30 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),

(1, 'AJUSTE', 5, (CURRENT_DATE - INTERVAL '5 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(8, 'AJUSTE', 150, (CURRENT_DATE - INTERVAL '5 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(12, 'AJUSTE', 100, (CURRENT_DATE - INTERVAL '5 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física'),
(20, 'AJUSTE', 200, (CURRENT_DATE - INTERVAL '5 days' + TIME '10:00:00')::timestamp, 'Ajuste de inventário mensal - contagem física');

-- ============================================================================
-- 6. CONFIGURAÇÃO DO SALDO ATUAL E LOTES DE ESTOQUE (DEMO DE INSIGHTS)
-- ============================================================================
-- Configuração de insumos para os cenários dos relatórios preditivos:
-- - Pão Brioche (1): saldo 15 <= mínimo 20 (Crítico + Sugestão de Compra alta)
-- - Queijo Prato (3): saldo 25 <= mínimo 50 (Crítico + Lote vence em 2 dias -> Promoção Classic Burger)
-- - Bacon em Fatias (8): saldo 320 <= mínimo 500 (Crítico + Lote vence em 1 dia -> Promoção Bacon Supreme / Fritas)
-- - Sorvete de Morango (20): saldo 750 <= mínimo 1000 (Crítico + Lote vence em 3 dias -> Promoção Milkshake Morango)
-- - Alface Americana (4): saldo 600 <= mínimo 1000 (Crítico + Sugestão de Compra)
-- - Demais insumos: Saldo saudável e lotes regulares (15 a 90 dias)

UPDATE stock_items SET current_quantity = 15, minimum_stock = 20 WHERE id = 1;
UPDATE stock_items SET current_quantity = 80, minimum_stock = 20 WHERE id = 2;
UPDATE stock_items SET current_quantity = 25, minimum_stock = 50 WHERE id = 3;
UPDATE stock_items SET current_quantity = 600, minimum_stock = 1000 WHERE id = 4;
UPDATE stock_items SET current_quantity = 150, minimum_stock = 50 WHERE id = 5;
UPDATE stock_items SET current_quantity = 1500, minimum_stock = 500 WHERE id = 6;
UPDATE stock_items SET current_quantity = 2000, minimum_stock = 500 WHERE id = 7;
UPDATE stock_items SET current_quantity = 320, minimum_stock = 500 WHERE id = 8;
UPDATE stock_items SET current_quantity = 800, minimum_stock = 200 WHERE id = 9;
UPDATE stock_items SET current_quantity = 120, minimum_stock = 30 WHERE id = 10;
UPDATE stock_items SET current_quantity = 200, minimum_stock = 50 WHERE id = 11;
UPDATE stock_items SET current_quantity = 800, minimum_stock = 200 WHERE id = 12;
UPDATE stock_items SET current_quantity = 1600, minimum_stock = 500 WHERE id = 13;
UPDATE stock_items SET current_quantity = 70, minimum_stock = 20 WHERE id = 14;
UPDATE stock_items SET current_quantity = 800, minimum_stock = 200 WHERE id = 15;
UPDATE stock_items SET current_quantity = 65, minimum_stock = 20 WHERE id = 16;
UPDATE stock_items SET current_quantity = 1200, minimum_stock = 300 WHERE id = 17;
UPDATE stock_items SET current_quantity = 8500, minimum_stock = 2000 WHERE id = 18;
UPDATE stock_items SET current_quantity = 800, minimum_stock = 200 WHERE id = 19;
UPDATE stock_items SET current_quantity = 750, minimum_stock = 1000 WHERE id = 20;
UPDATE stock_items SET current_quantity = 7000, minimum_stock = 2000 WHERE id = 21;
UPDATE stock_items SET current_quantity = 1500, minimum_stock = 500 WHERE id = 22;
UPDATE stock_items SET current_quantity = 800, minimum_stock = 200 WHERE id = 23;
UPDATE stock_items SET current_quantity = 4000, minimum_stock = 1000 WHERE id = 24;
UPDATE stock_items SET current_quantity = 400, minimum_stock = 100 WHERE id = 25;
UPDATE stock_items SET current_quantity = 3800, minimum_stock = 1000 WHERE id = 26;
UPDATE stock_items SET current_quantity = 400, minimum_stock = 100 WHERE id = 27;
UPDATE stock_items SET current_quantity = 160, minimum_stock = 50 WHERE id = 28;
UPDATE stock_items SET current_quantity = 140, minimum_stock = 50 WHERE id = 29;
UPDATE stock_items SET current_quantity = 130, minimum_stock = 50 WHERE id = 30;

-- Inserção dos lotes correspondentes mantendo a invariante SUM(quantity) = current_quantity
INSERT INTO stock_lots (stock_item_id, quantity, expires_at, received_at) VALUES
(1, 15, CURRENT_DATE + INTERVAL '7 days', NOW() - INTERVAL '1 day'),
(2, 80, CURRENT_DATE + INTERVAL '25 days', NOW() - INTERVAL '2 days'),
(3, 25, CURRENT_DATE + INTERVAL '2 days', NOW() - INTERVAL '5 days'),   -- Expira em 2 dias (Promoção)
(4, 600, CURRENT_DATE + INTERVAL '4 days', NOW() - INTERVAL '2 days'),
(5, 150, CURRENT_DATE + INTERVAL '8 days', NOW() - INTERVAL '1 day'),
(6, 1500, CURRENT_DATE + INTERVAL '30 days', NOW() - INTERVAL '3 days'),
(7, 2000, CURRENT_DATE + INTERVAL '40 days', NOW() - INTERVAL '3 days'),
(8, 320, CURRENT_DATE + INTERVAL '1 day', NOW() - INTERVAL '6 days'),    -- Expira em 1 dia (Promoção)
(9, 800, CURRENT_DATE + INTERVAL '20 days', NOW() - INTERVAL '2 days'),
(10, 120, CURRENT_DATE + INTERVAL '30 days', NOW() - INTERVAL '1 day'),
(11, 200, CURRENT_DATE + INTERVAL '35 days', NOW() - INTERVAL '3 days'),
(12, 800, CURRENT_DATE + INTERVAL '60 days', NOW() - INTERVAL '4 days'),
(13, 1600, CURRENT_DATE + INTERVAL '60 days', NOW() - INTERVAL '4 days'),
(14, 70, CURRENT_DATE + INTERVAL '20 days', NOW() - INTERVAL '1 day'),
(15, 800, CURRENT_DATE + INTERVAL '25 days', NOW() - INTERVAL '2 days'),
(16, 65, CURRENT_DATE + INTERVAL '20 days', NOW() - INTERVAL '1 day'),
(17, 1200, CURRENT_DATE + INTERVAL '10 days', NOW() - INTERVAL '1 day'),
(18, 8500, CURRENT_DATE + INTERVAL '60 days', NOW() - INTERVAL '3 days'),
(19, 800, CURRENT_DATE + INTERVAL '45 days', NOW() - INTERVAL '2 days'),
(20, 750, CURRENT_DATE + INTERVAL '3 days', NOW() - INTERVAL '5 days'),   -- Expira em 3 dias (Promoção)
(21, 7000, CURRENT_DATE + INTERVAL '12 days', NOW() - INTERVAL '1 day'),
(22, 1500, CURRENT_DATE + INTERVAL '20 days', NOW() - INTERVAL '2 days'),
(23, 800, CURRENT_DATE + INTERVAL '45 days', NOW() - INTERVAL '3 days'),
(24, 4000, CURRENT_DATE + INTERVAL '50 days', NOW() - INTERVAL '4 days'),
(25, 400, CURRENT_DATE + INTERVAL '60 days', NOW() - INTERVAL '4 days'),
(26, 3800, CURRENT_DATE + INTERVAL '50 days', NOW() - INTERVAL '4 days'),
(27, 400, CURRENT_DATE + INTERVAL '90 days', NOW() - INTERVAL '5 days'),
(28, 160, CURRENT_DATE + INTERVAL '120 days', NOW() - INTERVAL '5 days'),
(29, 140, CURRENT_DATE + INTERVAL '120 days', NOW() - INTERVAL '5 days'),
(30, 130, CURRENT_DATE + INTERVAL '120 days', NOW() - INTERVAL '5 days');

-- ============================================================================
-- 7. ATUALIZAÇÃO DAS SEQUÊNCIAS
-- ============================================================================

SELECT setval(
    pg_get_serial_sequence('stock_lots', 'id'),
    GREATEST(1, (SELECT COALESCE(MAX(id), 1) FROM stock_lots))
);

SELECT setval(
    pg_get_serial_sequence('stock_movements', 'id'),
    GREATEST(1, (SELECT COALESCE(MAX(id), 1) FROM stock_movements))
);

COMMIT;
