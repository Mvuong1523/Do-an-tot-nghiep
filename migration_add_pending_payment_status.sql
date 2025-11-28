-- Migration: Add PENDING_PAYMENT status and payment_method to orders table
-- Date: 2025-11-27

USE web2;

-- 1. Cập nhật column status để chứa giá trị mới
ALTER TABLE orders 
MODIFY COLUMN status ENUM(
    'PENDING_PAYMENT',
    'PENDING', 
    'CONFIRMED', 
    'PROCESSING', 
    'SHIPPING', 
    'DELIVERED', 
    'COMPLETED', 
    'CANCELLED', 
    'RETURNED'
) NOT NULL DEFAULT 'PENDING';

-- 2. Thêm column payment_method
ALTER TABLE orders 
ADD COLUMN payment_method VARCHAR(20) NULL AFTER payment_status;

-- 2.1. Xóa column paid_at (trùng với Payment.paidAt)
-- Note: Nếu cần giữ dữ liệu cũ, backup trước khi xóa
ALTER TABLE orders 
DROP COLUMN IF EXISTS paid_at;

-- 3. Cập nhật dữ liệu cũ (nếu có)
-- Giả sử đơn cũ không có payment_id là COD
UPDATE orders 
SET payment_method = CASE 
    WHEN payment_id IS NULL THEN 'COD'
    ELSE 'SEPAY'
END
WHERE payment_method IS NULL;

-- 4. Kiểm tra kết quả
SELECT COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'web2' 
  AND TABLE_NAME = 'orders' 
  AND COLUMN_NAME = 'status';

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'web2' 
  AND TABLE_NAME = 'orders' 
  AND COLUMN_NAME IN ('status', 'payment_method');

-- Expected output:
-- status: enum('PENDING_PAYMENT','PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','COMPLETED','CANCELLED','RETURNED')
-- payment_method: varchar(20), YES
