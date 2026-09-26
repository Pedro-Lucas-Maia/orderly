-- 1. Criação do Estoque
INSERT INTO stocks (id, name) VALUES (1, 'Estoque Principal');

-- 2. Criação dos Insumos (Stock Items)
INSERT INTO stock_items (id, stock_id, name, category, unit, current_quantity, minimum_stock, unit_cost, active) VALUES
(1, 1, 'Pão Brioche Artesanal', 'Panificação', 'UNIDADE', 100, 20, 1.50, true),
(2, 1, 'Hambúrguer de Carne 160g', 'Carnes', 'UNIDADE', 100, 20, 4.00, true),
(3, 1, 'Queijo Prato (Fatia)', 'Laticínios', 'UNIDADE', 200, 50, 0.50, true),
(4, 1, 'Alface Americana', 'Hortifruti', 'GRAMA', 5000, 1000, 0.02, true),
(5, 1, 'Tomate (Rodela)', 'Hortifruti', 'UNIDADE', 300, 50, 0.20, true),
(6, 1, 'Maionese Especial da Casa', 'Molhos', 'MILILITRO', 2000, 500, 0.03, true),
(7, 1, 'Creme de Cheddar', 'Laticínios', 'GRAMA', 3000, 500, 0.04, true),
(8, 1, 'Bacon em Fatias', 'Carnes', 'GRAMA', 2000, 500, 0.05, true),
(9, 1, 'Cebola Caramelizada', 'Hortifruti', 'GRAMA', 1000, 200, 0.03, true),
(10, 1, 'Smash Burger Bovina 90g', 'Carnes', 'UNIDADE', 150, 30, 2.50, true),
(11, 1, 'American Cheese (Fatia)', 'Laticínios', 'UNIDADE', 300, 50, 0.60, true),
(12, 1, 'Picles', 'Hortifruti', 'GRAMA', 1000, 200, 0.04, true),
(13, 1, 'Ketchup', 'Molhos', 'MILILITRO', 2000, 500, 0.02, true),
(14, 1, 'Sobrecoxa de Frango Empanada', 'Carnes', 'UNIDADE', 80, 20, 3.50, true),
(15, 1, 'Maionese de Limão', 'Molhos', 'MILILITRO', 1000, 200, 0.03, true),
(16, 1, 'Frango Empanado Apimentado', 'Carnes', 'UNIDADE', 80, 20, 3.80, true),
(17, 1, 'Salada Coleslaw', 'Hortifruti', 'GRAMA', 1500, 300, 0.03, true),
(18, 1, 'Batata Frita Palito', 'Congelados', 'GRAMA', 10000, 2000, 0.015, true),
(19, 1, 'Farofa de Bacon', 'Carnes', 'GRAMA', 1000, 200, 0.06, true),
(20, 1, 'Sorvete de Morango', 'Sobremesas', 'GRAMA', 5000, 1000, 0.02, true),
(21, 1, 'Leite Integral', 'Laticínios', 'MILILITRO', 10000, 2000, 0.004, true),
(22, 1, 'Chantilly', 'Laticínios', 'GRAMA', 2000, 500, 0.03, true),
(23, 1, 'Calda de Morango', 'Sobremesas', 'MILILITRO', 1000, 200, 0.02, true),
(24, 1, 'Sorvete de Chocolate Belga', 'Sobremesas', 'GRAMA', 5000, 1000, 0.03, true),
(25, 1, 'Raspas de Chocolate Meio Amargo', 'Sobremesas', 'GRAMA', 500, 100, 0.08, true),
(26, 1, 'Sorvete de Creme', 'Sobremesas', 'GRAMA', 5000, 1000, 0.02, true),
(27, 1, 'Fava de Baunilha (Extrato)', 'Sobremesas', 'MILILITRO', 500, 100, 0.10, true),
(28, 1, 'Lata Coca-Cola 350ml', 'Bebidas', 'UNIDADE', 200, 50, 2.50, true),
(29, 1, 'Lata Guaraná Antarctica 350ml', 'Bebidas', 'UNIDADE', 200, 50, 2.30, true),
(30, 1, 'Lata Sprite 350ml', 'Bebidas', 'UNIDADE', 200, 50, 2.30, true);

-- 3. Criação dos Produtos com image_url (cruzando com cardapio.txt e cardapio_seed.md)
INSERT INTO products (id, name, description, price, active, image_url) VALUES
(1, 'Classic Burger', 'O clássico que não tem erro. Pão brioche artesanal, suculento hambúrguer de carne bovina 160g, queijo prato derretido, alface americana fresca, rodelas de tomate e a nossa maionese especial da casa.', 28.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790430780/classicBurguer.jpg'),
(2, 'Bacon Supreme', 'Para os amantes de bacon. Pão brioche, hambúrguer de carne bovina 160g, creme de cheddar derretido, fatias generosas de bacon crocante e cebola caramelizada.', 34.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790430948/baconSupreme.jpg'),
(3, 'Double Smash', 'Sabor em dobro com aquela crostinha perfeita. Pão brioche, dois smash burgers bovinos de 90g cada, duas fatias de american cheese, picles e ketchup.', 32.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431109/doubleSmash.jpg'),
(4, 'Crispy Chicken', 'Crocância é a palavra. Pão brioche, sobrecoxa de frango empanada super crocante, alface americana e maionese refrescante de limão.', 29.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431102/crispyChicken.jpg'),
(5, 'Spicy Chicken', 'Para quem gosta de um toque picante. Pão brioche, frango empanado levemente apimentado, salada coleslaw cremosa e picles.', 31.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431349/spicyChicken.jpg'),
(6, 'Batata Frita Tradicional', 'Porção individual de batatas fritas palito, douradas, sequinhas e super crocantes. Temperadas com sal marinho.', 14.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431344/batataFrita.jpg'),
(7, 'Batata Frita com Cheddar e Bacon', 'A nossa batata frita tradicional coberta com uma camada generosa de creme de queijo cheddar derretido e farofa de bacon crocante.', 24.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431413/batataCheddarBacon.jpg'),
(8, 'Milkshake de Morango', 'Clássico e refrescante. Sorvete de morango batido com leite, finalizado com chantilly e calda de morango. Copo de 400ml.', 22.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431586/milkshakeMorango.jpg'),
(9, 'Milkshake de Chocolate', 'Para os chocólatras. Sorvete de chocolate belga, finalizado com chantilly e raspas de chocolate meio amargo. Copo de 400ml.', 24.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431587/milkshakeChocolate.jpg'),
(10, 'Milkshake de Baunilha', 'A perfeição da simplicidade. Sorvete de creme com fava de baunilha autêntica, finalizado com chantilly. Copo de 400ml.', 21.90, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431808/milkshakeBaunilha.jpg'),
(11, 'Coca-Cola', 'Lata de Coca-Cola 350ml, servida bem gelada.', 7.00, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431802/cocaCola.jpg'),
(12, 'Guaraná Antarctica', 'Lata de Guaraná 350ml, servida bem gelada.', 7.00, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431801/guaranaAntartica.jpg'),
(13, 'Sprite', 'Lata de Sprite 350ml, servida bem gelada.', 7.00, true, 'https://res.cloudinary.com/xohadyyk/image/upload/v1790431878/sprite.jpg');

-- 4. Criação das Composições dos Produtos (Receitas)
-- Classic Burger (id: 1)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(1, 1, 1),   -- 1 Pão
(1, 2, 1),   -- 1 Hambúrguer 160g
(1, 3, 2),   -- 2 Fatias Queijo Prato
(1, 4, 30),  -- 30g Alface
(1, 5, 2),   -- 2 Rodelas Tomate
(1, 6, 20);  -- 20ml Maionese

-- Bacon Supreme (id: 2)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(2, 1, 1),   -- 1 Pão
(2, 2, 1),   -- 1 Hambúrguer 160g
(2, 7, 50),  -- 50g Cheddar
(2, 8, 40),  -- 40g Bacon
(2, 9, 30);  -- 30g Cebola Caramelizada

-- Double Smash (id: 3)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(3, 1, 1),   -- 1 Pão
(3, 10, 2),  -- 2 Smash 90g
(3, 11, 2),  -- 2 Fatias American Cheese
(3, 12, 15), -- 15g Picles
(3, 13, 20); -- 20ml Ketchup

-- Crispy Chicken (id: 4)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(4, 1, 1),   -- 1 Pão
(4, 14, 1),  -- 1 Sobrecoxa Empanada
(4, 4, 30),  -- 30g Alface
(4, 15, 20); -- 20ml Maionese Limão

-- Spicy Chicken (id: 5)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(5, 1, 1),   -- 1 Pão
(5, 16, 1),  -- 1 Frango Apimentado
(5, 17, 40), -- 40g Coleslaw
(5, 12, 15); -- 15g Picles

-- Batata Frita Tradicional (id: 6)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(6, 18, 150); -- 150g Batata Frita

-- Batata Frita com Cheddar e Bacon (id: 7)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(7, 18, 200), -- 200g Batata Frita
(7, 7, 80),   -- 80g Cheddar
(7, 19, 30);  -- 30g Farofa de Bacon

-- Milkshake Morango (id: 8)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(8, 20, 200), -- 200g Sorvete Morango
(8, 21, 100), -- 100ml Leite
(8, 22, 30),  -- 30g Chantilly
(8, 23, 20);  -- 20ml Calda

-- Milkshake Chocolate (id: 9)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(9, 24, 200), -- 200g Sorvete Chocolate
(9, 21, 100), -- 100ml Leite
(9, 22, 30),  -- 30g Chantilly
(9, 25, 10);  -- 10g Raspas

-- Milkshake Baunilha (id: 10)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(10, 26, 200), -- 200g Sorvete Creme
(10, 21, 100), -- 100ml Leite
(10, 22, 30),  -- 30g Chantilly
(10, 27, 5);   -- 5ml Extrato Baunilha

-- Bebidas (11, 12, 13)
INSERT INTO product_compositions (product_id, stock_item_id, quantity) VALUES
(11, 28, 1), -- 1 Lata Coca
(12, 29, 1), -- 1 Lata Guaraná
(13, 30, 1); -- 1 Lata Sprite

-- 5. Atualização das sequences (Importante pois fizemos INSERT forçando o ID)
SELECT setval('stocks_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stocks));
SELECT setval('stock_items_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stock_items));
SELECT setval('products_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products));
SELECT setval('product_compositions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM product_compositions));
