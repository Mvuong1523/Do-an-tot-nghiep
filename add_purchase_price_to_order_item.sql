-- Migration: Thêm cột purchase_price vào bảng order_item
-- Mục đích: Lưu giá vốn tại thời điểm bán để tính lợi nhuận chính xác

-- 1. Thêm cột purchase_price
ALTER TABLE order_item 
ADD COLUMN purchase_price DOUBLE DEFAULT 0 COMMENT 'Giá nhập tại thời điểm bán';

-- 2. Cập nhật giá vốn cho các đơn hàng cũ
-- Giả định giá vốn = 60% giá bán (có thể điều chỉnh theo thực tế)
UPDATE order_item 
SET purchase_price = price * 0.6 
WHERE purchase_price IS NULL OR purchase_price = 0;

-- 3. Kiểm tra kết quả
SELECT 
    oi.order_item_id,
    o.order_code,
    p.name as product_name,
    oi.quantity,
    oi.price as sale_price,
    oi.purchase_price,
    (oi.price - oi.purchase_price) as profit_per_unit,
    (oi.price - oi.purchase_price) * oi.quantity as total_profit
FROM order_item oi
JOIN orders o ON oi.order_id = o.order_id
JOIN product p ON oi.product_id = p.product_id
LIMIT 10;

-- 4. Thống kê lợi nhuận gộp
SELECT 
    DATE(o.created_at) as order_date,
    COUNT(DISTINCT o.order_id) as total_orders,
    SUM(oi.quantity * oi.price) as total_revenue,
    SUM(oi.quantity * oi.purchase_price) as total_cost,
    SUM(oi.quantity * (oi.price - oi.purchase_price)) as gross_profit,
    ROUND(SUM(oi.quantity * (oi.price - oi.purchase_price)) / SUM(oi.quantity * oi.price) * 100, 2) as profit_margin_percent
FROM order_item oi
JOIN orders o ON oi.order_id = o.order_id
WHERE o.payment_status = 'PAID'
GROUP BY DATE(o.created_at)
ORDER BY order_date DESC
LIMIT 30;
