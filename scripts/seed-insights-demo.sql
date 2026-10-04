-- Seed dummy para demo de insights (NÃO é migration Flyway).
-- Pode ser reexecutado: apaga vendas INSIGHTS_DEMO e reescreve saldo/lotes dos
-- insumos 1 (pão), 3 (queijo), 8 (bacon) e 20 (sorvete de morango).
-- Histórico de vendas e saldo atual são independentes (projeto acadêmico).

BEGIN;

DELETE FROM sale_items
WHERE sale_id IN (SELECT id FROM sales WHERE observation = 'INSIGHTS_DEMO');

DELETE FROM sales
WHERE observation = 'INSIGHTS_DEMO';

INSERT INTO stores (
    id, name, opening_time, closing_time,
    max_orders_in_progress, automatic_pause, status,
    address_street, address_number, address_neighborhood, address_city, address_zip_code
)
VALUES (
    2, 'Orderly Campus', '10:00:00', '22:00:00',
    30, true, 'ABERTA',
    'Av. Cap. Mor Gouveia', '3000', 'Lagoa Nova', 'Natal', '59078-900'
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    opening_time = EXCLUDED.opening_time,
    closing_time = EXCLUDED.closing_time;

SELECT setval(
    pg_get_serial_sequence('stores', 'id'),
    GREATEST(2, (SELECT COALESCE(MAX(id), 1) FROM stores))
);

WITH days AS (
    SELECT d::date AS sale_date
    FROM generate_series(
        CURRENT_DATE - INTERVAL '56 days',
        CURRENT_DATE - INTERVAL '1 day',
        INTERVAL '1 day'
    ) AS d
    WHERE EXTRACT(DOW FROM d) IN (2, 5)
),
store1 AS (
    INSERT INTO sales (id, store_id, date, status, total_value, observation, user_id)
    SELECT gen_random_uuid(),
           1,
           sale_date + TIME '18:30',
           'CONFIRMED',
           CASE
               WHEN EXTRACT(DOW FROM sale_date) = 2 THEN (18 * 28.90) + (10 * 34.90)
               ELSE (22 * 28.90) + (14 * 34.90)
           END,
           'INSIGHTS_DEMO',
           NULL
    FROM days
    RETURNING id, date
),
store2 AS (
    INSERT INTO sales (id, store_id, date, status, total_value, observation, user_id)
    SELECT gen_random_uuid(),
           2,
           sale_date + TIME '19:15',
           'CONFIRMED',
           CASE
               WHEN EXTRACT(DOW FROM sale_date) = 2 THEN (8 * 29.90) + (12 * 22.90)
               ELSE (10 * 29.90) + (16 * 22.90)
           END,
           'INSIGHTS_DEMO',
           NULL
    FROM days
    RETURNING id, date
)
INSERT INTO sale_items (id, sale_id, product_id, quantity, unit_price, subtotal)
SELECT gen_random_uuid(), s.id, 1,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 18 ELSE 22 END,
       28.90,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 18 ELSE 22 END * 28.90
FROM store1 s
UNION ALL
SELECT gen_random_uuid(), s.id, 2,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 10 ELSE 14 END,
       34.90,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 10 ELSE 14 END * 34.90
FROM store1 s
UNION ALL
SELECT gen_random_uuid(), s.id, 4,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 8 ELSE 10 END,
       29.90,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 8 ELSE 10 END * 29.90
FROM store2 s
UNION ALL
SELECT gen_random_uuid(), s.id, 8,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 12 ELSE 16 END,
       22.90,
       CASE WHEN EXTRACT(DOW FROM s.date) = 2 THEN 12 ELSE 16 END * 22.90
FROM store2 s;

-- Estoque baixo + lotes (compra, crítico, validade, promoção).
-- Pão 8 < mínimo 20; consumo terça loja 1 ≈ 18+10 = 28 → quantityToBuy 20.
-- Queijo 20 < mínimo 50; consumo Classic 18×2 = 36 → comprar 16; lote vence em 2 dias.
-- Bacon 120 < mínimo 500; consumo Bacon Supreme 10×40 = 400 → comprar 280; lote vence amanhã.
-- Sorvete morango 800 < mínimo 1000; consumo loja 2 terça 12×200 = 2400 → comprar 1600.

UPDATE stock_items SET current_quantity = 8, minimum_stock = 20 WHERE id = 1;
UPDATE stock_items SET current_quantity = 20, minimum_stock = 50 WHERE id = 3;
UPDATE stock_items SET current_quantity = 120, minimum_stock = 500 WHERE id = 8;
UPDATE stock_items SET current_quantity = 800, minimum_stock = 1000 WHERE id = 20;

DELETE FROM stock_lots WHERE stock_item_id IN (1, 3, 8, 20);

INSERT INTO stock_lots (stock_item_id, quantity, expires_at, received_at) VALUES
    (1, 8, CURRENT_DATE + 14, NOW()),
    (3, 20, CURRENT_DATE + 2, NOW()),
    (8, 120, CURRENT_DATE + 1, NOW()),
    (20, 800, CURRENT_DATE + 30, NOW());

SELECT setval(
    pg_get_serial_sequence('stock_lots', 'id'),
    GREATEST(1, (SELECT COALESCE(MAX(id), 1) FROM stock_lots))
);

COMMIT;
