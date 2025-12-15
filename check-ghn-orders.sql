-- ============================================
-- SQL Script kiểm tra tích hợp GHN
-- ============================================

-- 1. Kiểm tra các đơn hàng có mã GHN
SELECT 
    id,
    order_code,
    status,
    payment_status,
    shipping_fee,
    total,
    ghn_order_code,
    ghn_shipping_status,
    ghn_created_at,
    ghn_expected_delivery_time,
    created_at
FROM orders
WHERE ghn_order_code IS NOT NULL
ORDER BY created_at DESC
LIMIT 20;

-- 2. Kiểm tra các đơn hàng KHÔNG có mã GHN (nội thành HN)
SELECT 
    id,
    order_code,
    status,
    shipping_fee,
    shipping_address,
    created_at
FROM orders
WHERE ghn_order_code IS NULL
ORDER BY created_at DESC
LIMIT 20;

-- 3. Thống kê đơn hàng theo GHN status
SELECT 
    ghn_shipping_status,
    COUNT(*) as total_orders,
    SUM(total) as total_revenue
FROM orders
WHERE ghn_order_code IS NOT NULL
GROUP BY ghn_shipping_status
ORDER BY total_orders DESC;

-- 4. Kiểm tra đơn hàng mới nhất
SELECT 
    order_code,
    status,
    payment_method,
    shipping_fee,
    total,
    ghn_order_code,
    ghn_shipping_status,
    created_at
FROM orders
ORDER BY created_at DESC
LIMIT 5;

-- 5. Tìm đơn hàng theo mã GHN
-- Thay 'GHNABCD1234' bằng mã GHN thực tế
SELECT 
    o.id,
    o.order_code,
    o.status,
    o.payment_status,
    o.total,
    o.ghn_order_code,
    o.ghn_shipping_status,
    o.ghn_created_at,
    o.ghn_expected_delivery_time,
    c.full_name as customer_name,
    c.phone as customer_phone
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.ghn_order_code = 'GHNABCD1234';

-- 6. Kiểm tra đơn hàng đã giao (delivered)
SELECT 
    order_code,
    ghn_order_code,
    ghn_shipping_status,
    status,
    payment_status,
    delivered_at,
    total
FROM orders
WHERE ghn_shipping_status = 'delivered'
ORDER BY delivered_at DESC;

-- 7. Kiểm tra đơn hàng có vấn đề (exception, damage, lost)
SELECT 
    order_code,
    ghn_order_code,
    ghn_shipping_status,
    status,
    shipping_address,
    created_at
FROM orders
WHERE ghn_shipping_status IN ('exception', 'damage', 'lost', 'returned')
ORDER BY created_at DESC;

-- 8. So sánh đơn có GHN vs không có GHN
SELECT 
    CASE 
        WHEN ghn_order_code IS NOT NULL THEN 'Có GHN'
        ELSE 'Không có GHN'
    END as ghn_type,
    COUNT(*) as total_orders,
    AVG(shipping_fee) as avg_shipping_fee,
    SUM(total) as total_revenue
FROM orders
GROUP BY ghn_type;

-- 9. Timeline đơn hàng GHN (từ tạo đến giao)
SELECT 
    order_code,
    ghn_order_code,
    created_at,
    confirmed_at,
    ghn_created_at,
    shipped_at,
    delivered_at,
    TIMESTAMPDIFF(HOUR, ghn_created_at, delivered_at) as delivery_hours
FROM orders
WHERE ghn_order_code IS NOT NULL 
  AND delivered_at IS NOT NULL
ORDER BY created_at DESC
LIMIT 10;

-- 10. Kiểm tra đơn hàng theo địa chỉ
SELECT 
    order_code,
    shipping_address,
    shipping_fee,
    ghn_order_code,
    CASE 
        WHEN ghn_order_code IS NOT NULL THEN 'GHN'
        ELSE 'Nội bộ'
    END as ship_method
FROM orders
ORDER BY created_at DESC
LIMIT 20;

-- 11. Đếm số đơn theo từng trạng thái GHN
SELECT 
    COALESCE(ghn_shipping_status, 'NULL') as ghn_status,
    status as order_status,
    COUNT(*) as count
FROM orders
GROUP BY ghn_shipping_status, status
ORDER BY count DESC;

-- 12. Tìm đơn hàng bị stuck (đã tạo GHN nhưng lâu chưa giao)
SELECT 
    order_code,
    ghn_order_code,
    ghn_shipping_status,
    ghn_created_at,
    ghn_expected_delivery_time,
    TIMESTAMPDIFF(DAY, ghn_created_at, NOW()) as days_since_created
FROM orders
WHERE ghn_order_code IS NOT NULL
  AND status NOT IN ('DELIVERED', 'CANCELLED')
  AND TIMESTAMPDIFF(DAY, ghn_created_at, NOW()) > 5
ORDER BY days_since_created DESC;

-- 13. Kiểm tra đơn hàng theo customer
-- Thay 1 bằng customer_id thực tế
SELECT 
    o.order_code,
    o.status,
    o.ghn_order_code,
    o.ghn_shipping_status,
    o.total,
    o.created_at,
    c.full_name,
    c.phone
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.customer_id = 1
ORDER BY o.created_at DESC;

-- 14. Xem chi tiết đơn hàng cụ thể (bao gồm items)
-- Thay 'ORD20231212001' bằng order_code thực tế
SELECT 
    o.order_code,
    o.status,
    o.payment_status,
    o.payment_method,
    o.subtotal,
    o.shipping_fee,
    o.total,
    o.ghn_order_code,
    o.ghn_shipping_status,
    o.shipping_address,
    oi.product_name,
    oi.quantity,
    oi.price,
    oi.subtotal as item_subtotal
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.order_code = 'ORD20231212001';

-- 15. Kiểm tra performance GHN (tỷ lệ giao thành công)
SELECT 
    COUNT(*) as total_ghn_orders,
    SUM(CASE WHEN ghn_shipping_status = 'delivered' THEN 1 ELSE 0 END) as delivered_count,
    SUM(CASE WHEN ghn_shipping_status IN ('returned', 'cancel') THEN 1 ELSE 0 END) as failed_count,
    ROUND(SUM(CASE WHEN ghn_shipping_status = 'delivered' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate
FROM orders
WHERE ghn_order_code IS NOT NULL;
