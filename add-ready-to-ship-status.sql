-- ===== THÊM STATUS MỚI: READY_TO_SHIP =====

-- 1. Kiểm tra các đơn hiện tại có status = SHIPPING
SELECT 
    id,
    order_code,
    status,
    ghn_order_code,
    shipped_at,
    created_at
FROM orders
WHERE status = 'SHIPPING'
ORDER BY created_at DESC;

-- 2. Update các đơn đã xuất kho nhưng chưa giao sang READY_TO_SHIP
-- (Các đơn có ghnOrderCode nhưng chưa được tài xế lấy)
UPDATE orders
SET status = 'READY_TO_SHIP'
WHERE status = 'SHIPPING'
  AND ghn_order_code IS NOT NULL
  AND delivered_at IS NULL;

-- 3. Kiểm tra kết quả
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
        WHEN 'SHIPPING' THEN 4
        WHEN 'DELIVERED' THEN 5
        WHEN 'COMPLETED' THEN 6
        ELSE 7
    END;

-- 4. Xem các đơn READY_TO_SHIP
SELECT 
    id,
    order_code,
    customer_name,
    ghn_order_code,
    shipped_at
FROM orders
WHERE status = 'READY_TO_SHIP'
ORDER BY shipped_at DESC;

-- NOTE: 
-- - READY_TO_SHIP: Đã xuất kho, đợi tài xế lấy hàng
-- - SHIPPING: Tài xế đã lấy hàng và đang giao
-- - Khi GHN webhook báo tài xế đã lấy hàng → Update sang SHIPPING
