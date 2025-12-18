-- Kiểm tra dữ liệu Module Kế toán

-- 1. Kiểm tra đơn hàng đã thanh toán
SELECT 
    COUNT(*) as total_orders,
    SUM(CASE WHEN payment_status = 'PAID' THEN 1 ELSE 0 END) as paid_orders,
    SUM(CASE WHEN status = 'CONFIRMED' AND payment_status = 'PAID' THEN 1 ELSE 0 END) as confirmed_paid,
    SUM(CASE WHEN status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_orders
FROM orders
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 2. Kiểm tra giao dịch tài chính
SELECT 
    type,
    category,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM financial_transactions
WHERE transaction_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY type, category;

-- 3. Kiểm tra công nợ nhà cung cấp
SELECT 
    status,
    COUNT(*) as count,
    SUM(total_amount) as total,
    SUM(paid_amount) as paid,
    SUM(remaining_amount) as remaining
FROM supplier_payables
GROUP BY status;

-- 4. Kiểm tra thanh toán NCC
SELECT 
    COUNT(*) as payment_count,
    SUM(amount) as total_paid
FROM supplier_payments
WHERE payment_date >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 5. Kiểm tra đối soát thanh toán
SELECT 
    gateway,
    status,
    COUNT(*) as count,
    SUM(gateway_amount) as gateway_total,
    SUM(system_amount) as system_total,
    SUM(discrepancy) as total_discrepancy
FROM payment_reconciliations
WHERE transaction_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY gateway, status;

-- 6. Chi tiết đơn hàng gần đây
SELECT 
    order_code,
    status,
    payment_status,
    subtotal,
    shipping_fee,
    total,
    created_at
FROM orders
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY created_at DESC
LIMIT 10;

-- 7. Chi tiết giao dịch tài chính gần đây
SELECT 
    order_id,
    type,
    category,
    amount,
    description,
    transaction_date
FROM financial_transactions
WHERE transaction_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY transaction_date DESC
LIMIT 10;

-- 8. Kiểm tra payments
SELECT 
    COUNT(*) as payment_count,
    SUM(amount) as total_amount,
    payment_method,
    status
FROM payments
WHERE paid_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY payment_method, status;

-- 9. Tổng quan doanh thu
SELECT 
    DATE(created_at) as date,
    COUNT(*) as order_count,
    SUM(total) as daily_revenue
FROM orders
WHERE payment_status = 'PAID'
  AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- 10. Kiểm tra phiếu nhập hàng
SELECT 
    po_code,
    status,
    received_date,
    supplier_id
FROM purchase_orders
WHERE received_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY received_date DESC
LIMIT 10;
