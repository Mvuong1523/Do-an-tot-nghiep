-- ===== THÊM 2 TRẠNG THÁI MỚI: PICKED_UP và DELIVERY_FAILED =====

-- 1. Kiểm tra độ dài cột status hiện tại
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'orders'
  AND COLUMN_NAME = 'status';

-- 2. Tăng độ dài cột status lên VARCHAR(20) nếu chưa đủ
ALTER TABLE orders 
MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- 3. Kiểm tra các trạng thái hiện có
SELECT 
    status,
    COUNT(*) as total
FROM orders
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'PENDING_PAYMENT' THEN 1
        WHEN 'CONFIRMED' THEN 2
        WHEN 'READY_TO_SHIP' THEN 3
        WHEN 'PICKED_UP' THEN 4
        WHEN 'SHIPPING' THEN 5
        WHEN 'DELIVERY_FAILED' THEN 6
        WHEN 'DELIVERED' THEN 7
        WHEN 'COMPLETED' THEN 8
        WHEN 'CANCELLED' THEN 9
        WHEN 'RETURNED' THEN 10
        ELSE 11
    END;

-- 4. Verify độ dài các enum values
-- PENDING_PAYMENT  (15 ký tự) - DÀI NHẤT
-- DELIVERY_FAILED  (15 ký tự) - MỚI
-- READY_TO_SHIP    (14 ký tự)
-- PICKED_UP        (9 ký tự) - MỚI
-- CONFIRMED        (9 ký tự)
-- SHIPPING         (8 ký tự)
-- DELIVERED        (9 ký tự)
-- COMPLETED        (9 ký tự)
-- CANCELLED        (9 ký tự)
-- RETURNED         (8 ký tự)
-- PROCESSING       (10 ký tự)

-- NOTE: 
-- PICKED_UP: Tài xế đã lấy hàng từ kho/bưu cục
-- DELIVERY_FAILED: Giao hàng thất bại (khách không nhận, sai địa chỉ, v.v.)
-- Flow: READY_TO_SHIP → PICKED_UP → SHIPPING → DELIVERED/DELIVERY_FAILED
