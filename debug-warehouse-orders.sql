-- ===== DEBUG WAREHOUSE ORDERS =====

-- 1. Xem tất cả đơn hàng
SELECT 
    id,
    order_code,
    status,
    payment_status,
    ghn_order_code,
    created_at,
    confirmed_at,
    shipped_at
FROM orders
ORDER BY created_at DESC
LIMIT 20;

-- 2. Đếm đơn hàng theo status
SELECT 
    status,
    COUNT(*) as total,
    COUNT(ghn_order_code) as with_ghn
FROM orders
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'PENDING_PAYMENT' THEN 1
        WHEN 'CONFIRMED' THEN 2
        WHEN 'SHIPPING' THEN 3
        WHEN 'DELIVERED' THEN 4
        WHEN 'COMPLETED' THEN 5
        ELSE 6
    END;

-- 3. Tìm đơn CONFIRMED (chờ xuất kho)
SELECT 
    id,
    order_code,
    customer_name,
    total,
    confirmed_at
FROM orders
WHERE status = 'CONFIRMED'
ORDER BY confirmed_at DESC;

-- 4. Tìm đơn SHIPPING (đã xuất kho)
SELECT 
    id,
    order_code,
    customer_name,
    total,
    ghn_order_code,
    shipped_at
FROM orders
WHERE status = 'SHIPPING'
ORDER BY shipped_at DESC;

-- 5. Tìm đơn có GHN nhưng status sai (BUG)
SELECT 
    id,
    order_code,
    status,
    ghn_order_code,
    shipped_at
FROM orders
WHERE ghn_order_code IS NOT NULL
  AND status != 'SHIPPING';

-- 6. Fix đơn có GHN nhưng status sai
-- UNCOMMENT để chạy fix
/*
UPDATE orders
SET 
    status = 'SHIPPING',
    shipped_at = COALESCE(shipped_at, ghn_created_at, NOW())
WHERE ghn_order_code IS NOT NULL
  AND status != 'SHIPPING';
*/

-- 7. Tạo đơn test CONFIRMED (nếu cần)
-- UNCOMMENT để tạo
/*
INSERT INTO orders (
    order_code, customer_id, status, payment_status, payment_method,
    shipping_address, province, district, ward, ward_name, address,
    subtotal, shipping_fee, discount, total,
    created_at, confirmed_at
)
VALUES (
    CONCAT('ORD_TEST_', UNIX_TIMESTAMP()),
    1,
    'CONFIRMED',
    'PAID',
    'COD',
    'Số 1, Phường Test, Quận Test, Hà Nội',
    'Hà Nội',
    'Hà Đông',
    '20308',
    'Phường Yên Hòa',
    'Số 1, Đường Test',
    1000000,
    30000,
    0,
    1030000,
    NOW(),
    NOW()
);
*/

-- 8. Tạo đơn test SHIPPING (nếu cần)
-- UNCOMMENT để tạo
/*
INSERT INTO orders (
    order_code, customer_id, status, payment_status, payment_method,
    shipping_address, province, district, ward, ward_name, address,
    subtotal, shipping_fee, discount, total,
    ghn_order_code, ghn_shipping_status,
    created_at, confirmed_at, shipped_at, ghn_created_at
)
VALUES (
    CONCAT('ORD_TEST_SHIP_', UNIX_TIMESTAMP()),
    1,
    'SHIPPING',
    'PAID',
    'COD',
    'Số 2, Phường Test, Quận Test, Hà Nội',
    'Hà Nội',
    'Hà Đông',
    '20308',
    'Phường Yên Hòa',
    'Số 2, Đường Test',
    2000000,
    30000,
    0,
    2030000,
    CONCAT('GHN_TEST_', UNIX_TIMESTAMP()),
    'created',
    NOW(),
    NOW(),
    NOW(),
    NOW()
);
*/

-- 9. Xem chi tiết một đơn cụ thể
SELECT * FROM orders WHERE id = 1;

-- 10. Kiểm tra customer_id có tồn tại không
SELECT id, full_name, phone FROM customers LIMIT 5;
