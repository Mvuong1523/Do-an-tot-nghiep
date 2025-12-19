-- Fix: Tăng độ dài cột status trong bảng orders
-- Lỗi: Data truncated for column 'status' at row 1
-- Nguyên nhân: Status mới READY_TO_SHIP dài 14 ký tự, cột hiện tại quá ngắn

-- Kiểm tra độ dài hiện tại
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'orders'
  AND COLUMN_NAME = 'status';

-- Tăng độ dài cột status lên VARCHAR(20) để chứa được tất cả các giá trị enum
ALTER TABLE orders 
MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- Verify lại
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'orders'
  AND COLUMN_NAME = 'status';

-- Kiểm tra các giá trị status hiện có
SELECT DISTINCT status, LENGTH(status) as length 
FROM orders 
ORDER BY length DESC;

-- Danh sách các OrderStatus enum values và độ dài:
-- PENDING_PAYMENT  (15 ký tự) ⚠️ DÀI NHẤT
-- READY_TO_SHIP    (14 ký tự)
-- CONFIRMED        (9 ký tự)
-- SHIPPING         (8 ký tự)
-- DELIVERED        (9 ký tự)
-- COMPLETED        (9 ký tự)
-- CANCELLED        (9 ký tự)
-- RETURNED         (8 ký tự)
-- PROCESSING       (10 ký tự)

-- Khuyến nghị: Dùng VARCHAR(20) để an toàn cho tương lai
