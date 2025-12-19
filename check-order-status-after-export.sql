-- Kiểm tra trạng thái đơn hàng sau khi xuất kho

-- 1. Xem các đơn hàng đã có GHN order code
SELECT 
    id,
    order_code,
    status,
    ghn_order_code,
    ghn_shipping_status,
    confirmed_at,
    shipped_at,
    ghn_created_at,
    created_at
FROM orders
WHERE ghn_order_code IS NOT NULL
ORDER BY created_at DESC;

-- 2. Kiểm tra đơn hàng có GHN nhưng status chưa đúng (BUG)
SELECT 
    id,
    order_code,
    status,
    ghn_order_code,
    shipped_at
FROM orders
WHERE ghn_order_code IS NOT NULL
  AND status != 'SHIPPING'  -- ❌ Lỗi: Có GHN nhưng không phải SHIPPING
ORDER BY created_at DESC;

-- 3. Xem timeline của một đơn hàng cụ thể
SELECT 
    order_code,
    status,
    created_at as 'Tạo đơn',
    confirmed_at as 'Xác nhận',
    shipped_at as 'Xuất kho/Giao hàng',
    delivered_at as 'Giao thành công',
    ghn_order_code as 'Mã GHN',
    ghn_created_at as 'Tạo đơn GHN'
FROM orders
WHERE id = 1;

-- 4. Thống kê đơn hàng theo trạng thái
SELECT 
    status,
    COUNT(*) as total,
    COUNT(ghn_order_code) as with_ghn,
    COUNT(CASE WHEN ghn_order_code IS NULL THEN 1 END) as without_ghn
FROM orders
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'PENDING_PAYMENT' THEN 1
        WHEN 'CONFIRMED' THEN 2
        WHEN 'SHIPPING' THEN 3
        WHEN 'DELIVERED' THEN 4
        WHEN 'COMPLETED' THEN 5
        WHEN 'CANCELLED' THEN 6
        ELSE 7
    END;

-- 5. Đơn hàng đang chờ xuất kho (CONFIRMED)
SELECT 
    id,
    order_code,
    customer_name,
    total,
    shipping_fee,
    province,
    district,
    confirmed_at,
    TIMESTAMPDIFF(HOUR, confirmed_at, NOW()) as hours_since_confirmed
FROM orders
WHERE status = 'CONFIRMED'
ORDER BY confirmed_at ASC;

-- 6. Đơn hàng đang giao (SHIPPING)
SELECT 
    id,
    order_code,
    customer_name,
    ghn_order_code,
    ghn_shipping_status,
    shipped_at,
    ghn_expected_delivery_time,
    TIMESTAMPDIFF(HOUR, shipped_at, NOW()) as hours_since_shipped
FROM orders
WHERE status = 'SHIPPING'
ORDER BY shipped_at DESC;

-- 7. Kiểm tra đơn nội thành (không dùng GHN)
SELECT 
    id,
    order_code,
    status,
    province,
    district,
    shipping_fee,
    ghn_order_code,
    shipped_at
FROM orders
WHERE (province LIKE '%Hà Nội%' OR province LIKE '%Ha Noi%')
  AND (district LIKE '%Hà Đông%' OR district LIKE '%Ha Dong%')
ORDER BY created_at DESC;

-- 8. Kiểm tra đơn miễn phí ship
SELECT 
    id,
    order_code,
    status,
    shipping_fee,
    ghn_order_code,
    shipped_at
FROM orders
WHERE shipping_fee = 0
ORDER BY created_at DESC;

-- 9. Fix manual cho đơn hàng có GHN nhưng status sai (nếu cần)
-- UNCOMMENT để chạy fix
/*
UPDATE orders
SET 
    status = 'SHIPPING',
    shipped_at = ghn_created_at
WHERE ghn_order_code IS NOT NULL
  AND status = 'CONFIRMED'
  AND shipped_at IS NULL;
*/

-- 10. Xem export orders và order tương ứng
SELECT 
    eo.export_code,
    eo.reason,
    eo.created_by,
    eo.export_date,
    o.order_code,
    o.status,
    o.ghn_order_code
FROM export_orders eo
LEFT JOIN orders o ON eo.export_code LIKE CONCAT('%', o.id, '%')
WHERE eo.reason = 'SALE'
ORDER BY eo.export_date DESC
LIMIT 20;
