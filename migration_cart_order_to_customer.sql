-- Migration: Chuyển Cart và Order từ User sang Customer
-- Ngày: 2025-11-26

-- =====================================================
-- BƯỚC 1: Backup dữ liệu (khuyến nghị chạy trước)
-- =====================================================
-- CREATE TABLE orders_backup AS SELECT * FROM orders;
-- CREATE TABLE carts_backup AS SELECT * FROM carts;

-- =====================================================
-- BƯỚC 2: Thêm cột mới customer_id
-- =====================================================

-- Thêm customer_id vào bảng orders
ALTER TABLE orders 
ADD COLUMN customer_id BIGINT;

-- Thêm customer_id vào bảng carts
ALTER TABLE carts 
ADD COLUMN customer_id BIGINT;

-- =====================================================
-- BƯỚC 3: Migrate dữ liệu từ user_id sang customer_id
-- =====================================================

-- Migrate orders: lấy customer_id từ user_id
UPDATE orders o
SET customer_id = (
    SELECT c.id 
    FROM customers c 
    WHERE c.user_id = o.user_id
)
WHERE o.user_id IS NOT NULL;

-- Migrate carts: lấy customer_id từ user_id
UPDATE carts ca
SET customer_id = (
    SELECT c.id 
    FROM customers c 
    WHERE c.user_id = ca.user_id
)
WHERE ca.user_id IS NOT NULL;

-- =====================================================
-- BƯỚC 4: Kiểm tra dữ liệu sau khi migrate
-- =====================================================

-- Kiểm tra orders có customer_id NULL không
SELECT COUNT(*) as orders_without_customer 
FROM orders 
WHERE customer_id IS NULL;

-- Kiểm tra carts có customer_id NULL không
SELECT COUNT(*) as carts_without_customer 
FROM carts 
WHERE customer_id IS NULL;

-- Nếu có NULL, cần xử lý trước khi tiếp tục!
-- Có thể xóa hoặc gán customer_id phù hợp

-- =====================================================
-- BƯỚC 5: Xóa cột user_id cũ và constraints
-- =====================================================

-- Xóa foreign key constraints cũ (nếu có)
-- MySQL/MariaDB:
-- ALTER TABLE orders DROP FOREIGN KEY orders_user_id_fk;
-- ALTER TABLE carts DROP FOREIGN KEY carts_user_id_fk;

-- PostgreSQL:
-- ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_user_id_fkey;
-- ALTER TABLE carts DROP CONSTRAINT IF EXISTS carts_user_id_fkey;

-- Xóa cột user_id
ALTER TABLE orders DROP COLUMN user_id;
ALTER TABLE carts DROP COLUMN user_id;

-- =====================================================
-- BƯỚC 6: Thêm constraints mới cho customer_id
-- =====================================================

-- Orders: thêm NOT NULL và foreign key
ALTER TABLE orders 
MODIFY COLUMN customer_id BIGINT NOT NULL;

ALTER TABLE orders 
ADD CONSTRAINT fk_orders_customer 
FOREIGN KEY (customer_id) REFERENCES customers(id);

-- Carts: thêm NOT NULL, UNIQUE và foreign key
ALTER TABLE carts 
MODIFY COLUMN customer_id BIGINT NOT NULL;

ALTER TABLE carts 
ADD CONSTRAINT fk_carts_customer 
FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE carts 
ADD CONSTRAINT uk_carts_customer 
UNIQUE (customer_id);

-- =====================================================
-- BƯỚC 7: Xóa các cột không cần thiết trong orders
-- =====================================================

ALTER TABLE orders 
DROP COLUMN customer_name,
DROP COLUMN customer_phone,
DROP COLUMN customer_email;

-- =====================================================
-- BƯỚC 8: Tạo indexes để tối ưu performance
-- =====================================================

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_carts_customer_id ON carts(customer_id);

-- =====================================================
-- HOÀN TẤT!
-- =====================================================

-- Kiểm tra lại cấu trúc bảng
DESCRIBE orders;
DESCRIBE carts;

-- Kiểm tra dữ liệu
SELECT COUNT(*) as total_orders FROM orders;
SELECT COUNT(*) as total_carts FROM carts;
