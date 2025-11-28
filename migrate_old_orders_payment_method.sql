-- Migrate Old Orders - Set Payment Method
-- Date: 2025-11-27
-- Purpose: Cập nhật payment_method cho các đơn cũ

USE web2;

-- ============================================
-- Phân tích đơn cũ
-- ============================================

-- 1. Xem các đơn chưa có payment_method
SELECT 
    COUNT(*) as total,
    status,
    payment_status,
    CASE 
        WHEN payment_id IS NULL THEN 'Likely COD'
        ELSE 'Likely Online'
    END as guessed_method
FROM orders
WHERE payment_method IS NULL
GROUP BY status, payment_status, guessed_method;

-- ============================================
-- Cập nhật payment_method cho đơn cũ
-- ============================================

-- 2.1. Đơn có payment_id → SEPAY
UPDATE orders 
SET payment_method = 'SEPAY'
WHERE payment_method IS NULL
  AND payment_id IS NOT NULL;

-- 2.2. Đơn không có payment_id → COD
UPDATE orders 
SET payment_method = 'COD'
WHERE payment_method IS NULL
  AND payment_id IS NULL;

-- ============================================
-- Cập nhật status cho đơn cũ (Nếu cần)
-- ============================================

-- 3.1. Đơn PENDING + COD → CONFIRMED (vì COD tự động confirm)
UPDATE orders 
SET 
    status = 'CONFIRMED',
    confirmed_at = created_at
WHERE status = 'PENDING'
  AND payment_method = 'COD'
  AND confirmed_at IS NULL;

-- 3.2. Đơn PENDING + SEPAY + UNPAID → PENDING_PAYMENT (chờ thanh toán)
-- Chỉ áp dụng cho đơn mới (< 1 ngày)
UPDATE orders 
SET status = 'PENDING_PAYMENT'
WHERE status = 'PENDING'
  AND payment_method = 'SEPAY'
  AND payment_status = 'UNPAID'
  AND created_at > DATE_SUB(NOW(), INTERVAL 1 DAY);

-- 3.3. Đơn PENDING + SEPAY + UNPAID + Cũ → CANCELLED (quá hạn)
UPDATE orders 
SET 
    status = 'CANCELLED',
    cancelled_at = NOW(),
    cancel_reason = 'Tự động hủy - Quá hạn thanh toán'
WHERE status = 'PENDING'
  AND payment_method = 'SEPAY'
  AND payment_status = 'UNPAID'
  AND created_at < DATE_SUB(NOW(), INTERVAL 1 DAY);

-- ============================================
-- Kiểm tra kết quả
-- ============================================

-- 4.1. Thống kê theo payment_method
SELECT 
    payment_method,
    status,
    COUNT(*) as total,
    SUM(total) as total_amount
FROM orders
GROUP BY payment_method, status
ORDER BY payment_method, status;

-- 4.2. Xem các đơn đã cập nhật
SELECT 
    id,
    order_code,
    payment_method,
    status,
    payment_status,
    total,
    created_at
FROM orders
ORDER BY created_at DESC
LIMIT 20;

-- 4.3. Kiểm tra còn đơn nào chưa có payment_method
SELECT COUNT(*) as orders_without_payment_method
FROM orders
WHERE payment_method IS NULL;

-- ============================================
-- Rollback (Nếu cần)
-- ============================================

-- Nếu muốn rollback, uncomment các dòng dưới:
-- UPDATE orders SET payment_method = NULL WHERE payment_method IN ('COD', 'SEPAY');
-- UPDATE orders SET status = 'PENDING' WHERE cancel_reason = 'Tự động hủy - Quá hạn thanh toán';
