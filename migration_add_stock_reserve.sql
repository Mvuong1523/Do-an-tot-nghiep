-- Migration: Thêm chức năng giữ hàng và xuất kho

-- BƯỚC 1: Thêm cột reserved_quantity vào products
ALTER TABLE products ADD COLUMN reserved_quantity BIGINT DEFAULT 0;

-- BƯỚC 2: Thêm cột reserved và exported vào order_items
ALTER TABLE order_items ADD COLUMN reserved BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE order_items ADD COLUMN exported BOOLEAN DEFAULT FALSE NOT NULL;

-- BƯỚC 3: Thêm cột order_id vào export_orders
ALTER TABLE export_orders ADD COLUMN order_id BIGINT;
ALTER TABLE export_orders ADD CONSTRAINT fk_export_orders_order FOREIGN KEY (order_id) REFERENCES orders(id);
CREATE INDEX idx_export_orders_order_id ON export_orders(order_id);

-- BƯỚC 4: Update dữ liệu cũ (nếu có)
-- Set reserved = true cho các order items của orders đã CONFIRMED
UPDATE order_items oi
INNER JOIN orders o ON oi.order_id = o.id
SET oi.reserved = TRUE
WHERE o.status IN ('CONFIRMED', 'SHIPPING', 'DELIVERED');

-- Set exported = true cho các order items của orders đã SHIPPING hoặc DELIVERED
UPDATE order_items oi
INNER JOIN orders o ON oi.order_id = o.id
SET oi.exported = TRUE
WHERE o.status IN ('SHIPPING', 'DELIVERED');

-- HOÀN TẤT!
SELECT 'Migration completed!' as status;
