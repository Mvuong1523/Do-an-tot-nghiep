-- Quick Migration: Cart & Order to Customer
-- Chạy script này để migrate dữ liệu

-- BƯỚC 1: Thêm cột customer_id
ALTER TABLE orders ADD COLUMN customer_id BIGINT;
ALTER TABLE carts ADD COLUMN customer_id BIGINT;

-- BƯỚC 2: Migrate dữ liệu
UPDATE orders o
SET customer_id = (SELECT c.id FROM customers c WHERE c.user_id = o.user_id);

UPDATE carts ca
SET customer_id = (SELECT c.id FROM customers c WHERE c.user_id = ca.user_id);

-- BƯỚC 3: Kiểm tra (phải trả về 0)
SELECT COUNT(*) as orders_null FROM orders WHERE customer_id IS NULL;
SELECT COUNT(*) as carts_null FROM carts WHERE customer_id IS NULL;

-- BƯỚC 4: Xóa cột cũ
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS fk_orders_user;
ALTER TABLE carts DROP FOREIGN KEY IF EXISTS fk_carts_user;

ALTER TABLE orders DROP COLUMN user_id;
ALTER TABLE orders DROP COLUMN customer_name;
ALTER TABLE orders DROP COLUMN customer_phone;
ALTER TABLE orders DROP COLUMN customer_email;

ALTER TABLE carts DROP COLUMN user_id;

-- BƯỚC 5: Thêm constraints mới
ALTER TABLE orders 
    MODIFY customer_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE carts 
    MODIFY customer_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    ADD CONSTRAINT uk_carts_customer UNIQUE (customer_id);

-- BƯỚC 6: Tạo indexes
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_carts_customer_id ON carts(customer_id);

-- HOÀN TẤT!
SELECT 'Migration completed!' as status;
