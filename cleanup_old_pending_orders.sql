-- Cleanup Old Pending Orders
-- Date: 2025-11-27
-- Purpose: Xóa hoặc cập nhật các đơn hàng PENDING cũ

USE web2;

-- ============================================
-- OPTION 1: XÓA tất cả đơn PENDING cũ (Khuyến nghị cho test DB)
-- ============================================

-- 1.1. Xem trước sẽ xóa bao nhiêu đơn
SELECT 
    COUNT(*) as total_pending_orders,
    MIN(created_at) as oldest,
    MAX(created_at) as newest
FROM orders 
WHERE status = 'PENDING';

-- 1.2. Xem chi tiết các đơn sẽ xóa
SELECT 
    id, 
    order_code, 
    status, 
    payment_status,
    total,
    created_at
FROM orders 
WHERE status = 'PENDING'
ORDER BY created_at DESC;

-- 1.3. XÓA các đơn PENDING (CẢNH BÁO: Không thể hoàn tác!)
-- Uncomment dòng dưới để thực thi
-- DELETE FROM orders WHERE status = 'PENDING';

-- ============================================
-- OPTION 2: Chuyển sang CANCELLED (Giữ lại lịch sử)
-- ============================================

-- 2.1. Cập nhật các đơn PENDING cũ sang CANCELLED
UPDATE orders 
SET 
    status = 'CANCELLED',
    cancelled_at = NOW(),
    cancel_reason = 'Tự động hủy - Đơn test cũ'
WHERE status = 'PENDING'
  AND created_at < DATE_SUB(NOW(), INTERVAL 1 DAY); -- Chỉ hủy đơn cũ hơn 1 ngày

-- ============================================
-- OPTION 3: Xóa chọn lọc (An toàn hơn)
-- ============================================

-- 3.1. Xóa đơn PENDING không có payment (COD test)
DELETE FROM orders 
WHERE status = 'PENDING'
  AND payment_id IS NULL
  AND created_at < DATE_SUB(NOW(), INTERVAL 1 DAY);

-- 3.2. Xóa đơn PENDING có payment nhưng đã expired
DELETE o FROM orders o
LEFT JOIN payments p ON o.payment_id = p.id
WHERE o.status = 'PENDING'
  AND p.status = 'EXPIRED'
  AND o.created_at < DATE_SUB(NOW(), INTERVAL 1 DAY);

-- ============================================
-- OPTION 4: Backup trước khi xóa (An toàn nhất)
-- ============================================

-- 4.1. Tạo bảng backup
CREATE TABLE IF NOT EXISTS orders_backup_20251127 AS
SELECT * FROM orders WHERE status = 'PENDING';

-- 4.2. Kiểm tra backup
SELECT COUNT(*) as backed_up_orders FROM orders_backup_20251127;

-- 4.3. Sau đó mới xóa
-- DELETE FROM orders WHERE status = 'PENDING';

-- ============================================
-- Kiểm tra kết quả
-- ============================================

-- Đếm số đơn theo status
SELECT 
    status,
    COUNT(*) as total,
    SUM(total) as total_amount
FROM orders
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'PENDING_PAYMENT' THEN 1
        WHEN 'PENDING' THEN 2
        WHEN 'CONFIRMED' THEN 3
        WHEN 'SHIPPING' THEN 4
        WHEN 'DELIVERED' THEN 5
        WHEN 'CANCELLED' THEN 6
        ELSE 7
    END;

-- Xem đơn mới nhất
SELECT 
    id,
    order_code,
    status,
    payment_status,
    payment_method,
    total,
    created_at
FROM orders
ORDER BY created_at DESC
LIMIT 10;
