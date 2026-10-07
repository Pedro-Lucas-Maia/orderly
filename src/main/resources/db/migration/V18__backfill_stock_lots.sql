-- Backfill stock_lots for existing stock_items that have current_quantity > 0 but no lots.
-- This fixes the issue where the V10 seed data gave quantities but V17 didn't create lots for them.
INSERT INTO stock_lots (stock_item_id, quantity, expires_at, received_at)
SELECT id, current_quantity, CURRENT_DATE + INTERVAL '30 days', CURRENT_TIMESTAMP
FROM stock_items
WHERE current_quantity > 0
  AND id NOT IN (SELECT stock_item_id FROM stock_lots);
