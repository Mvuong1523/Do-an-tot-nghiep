-- Kiểm tra các đơn hàng có ward code nhưng chưa có ward name
SELECT 
    id,
    order_code,
    status,
    province,
    district,
    ward,
    ward_name,
    address,
    created_at
FROM orders
WHERE ward IS NOT NULL 
  AND (ward_name IS NULL OR ward_name = '')
ORDER BY created_at DESC;

-- Đếm số lượng đơn hàng cần fix
SELECT 
    COUNT(*) as total_orders_need_fix
FROM orders
WHERE ward IS NOT NULL 
  AND (ward_name IS NULL OR ward_name = '');

-- Kiểm tra các đơn hàng đã có ward name
SELECT 
    id,
    order_code,
    status,
    province,
    district,
    ward,
    ward_name,
    created_at
FROM orders
WHERE ward IS NOT NULL 
  AND ward_name IS NOT NULL 
  AND ward_name != ''
ORDER BY created_at DESC
LIMIT 10;

-- Thống kê tổng quan
SELECT 
    COUNT(*) as total_orders,
    SUM(CASE WHEN ward IS NOT NULL THEN 1 ELSE 0 END) as orders_with_ward_code,
    SUM(CASE WHEN ward_name IS NOT NULL AND ward_name != '' THEN 1 ELSE 0 END) as orders_with_ward_name,
    SUM(CASE WHEN ward IS NOT NULL AND (ward_name IS NULL OR ward_name = '') THEN 1 ELSE 0 END) as orders_need_fix
FROM orders;
