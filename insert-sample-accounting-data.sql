-- Script tạo dữ liệu mẫu để test Module Kế toán
-- Chạy script này để có dữ liệu test ngay lập tức

-- ============================================
-- QUAN TRỌNG: Thay đổi user_id phù hợp
-- ============================================
SET @test_user_id = 1; -- Thay bằng ID user thật trong hệ thống

-- ============================================
-- 1. TẠO ĐƠN HÀNG MẪU
-- ============================================
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal, shipping_fee, total, created_at, updated_at)
VALUES 
-- Đơn đã xác nhận và thanh toán
('ORD-SAMPLE-001', @test_user_id, 'CONFIRMED', 'PAID', 1500000, 30000, 1530000, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('ORD-SAMPLE-002', @test_user_id, 'CONFIRMED', 'PAID', 2500000, 30000, 2530000, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
('ORD-SAMPLE-003', @test_user_id, 'PROCESSING', 'PAID', 3200000, 30000, 3230000, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

-- Đơn đã giao
('ORD-SAMPLE-004', @test_user_id, 'DELIVERED', 'PAID', 1800000, 30000, 1830000, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('ORD-SAMPLE-005', @test_user_id, 'COMPLETED', 'PAID', 4500000, 30000, 4530000, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),

-- Đơn chưa thanh toán (không tính vào doanh thu)
('ORD-SAMPLE-006', @test_user_id, 'PENDING_PAYMENT', 'PENDING', 2000000, 30000, 2030000, NOW(), NOW());

-- ============================================
-- 2. TẠO PAYMENT TƯƠNG ỨNG
-- ============================================
INSERT INTO payments (order_id, amount, payment_method, status, paid_at, created_at)
SELECT 
    id,
    total,
    'BANK_TRANSFER',
    'COMPLETED',
    created_at,
    created_at
FROM orders 
WHERE order_code LIKE 'ORD-SAMPLE-%' 
  AND payment_status = 'PAID';

-- ============================================
-- 3. TẠO GIAO DỊCH TÀI CHÍNH (DOANH THU)
-- ============================================
-- Doanh thu từ sản phẩm
INSERT INTO financial_transactions (order_id, type, category, amount, description, transaction_date, created_at)
SELECT 
    order_code,
    'REVENUE',
    'SALES',
    subtotal,
    CONCAT('Doanh thu bán hàng - ', order_code),
    created_at,
    created_at
FROM orders
WHERE order_code LIKE 'ORD-SAMPLE-%' 
  AND payment_status = 'PAID';

-- Doanh thu từ phí ship
INSERT INTO financial_transactions (order_id, type, category, amount, description, transaction_date, created_at)
SELECT 
    order_code,
    'REVENUE',
    'SHIPPING',
    shipping_fee,
    CONCAT('Phí vận chuyển - ', order_code),
    created_at,
    created_at
FROM orders
WHERE order_code LIKE 'ORD-SAMPLE-%' 
  AND payment_status = 'PAID';

-- Chi phí phí thanh toán (giả sử 1% tổng đơn)
INSERT INTO financial_transactions (order_id, type, category, amount, description, transaction_date, created_at)
SELECT 
    order_code,
    'EXPENSE',
    'PAYMENT_FEE',
    ROUND(total * 0.01, 0),
    CONCAT('Phí thanh toán - ', order_code),
    created_at,
    created_at
FROM orders
WHERE order_code LIKE 'ORD-SAMPLE-%' 
  AND payment_status = 'PAID';

-- ============================================
-- 4. TẠO DỮ LIỆU ĐỐI SOÁT THANH TOÁN
-- ============================================
-- Trường hợp 1: Khớp hoàn toàn
INSERT INTO payment_reconciliations (order_id, transaction_id, gateway, gateway_amount, system_amount, discrepancy, status, transaction_date, created_at)
SELECT 
    order_code,
    CONCAT('SEPAY-TXN-', LPAD(id, 6, '0')),
    'SEPAY',
    total,
    total,
    0,
    'MATCHED',
    created_at,
    created_at
FROM orders
WHERE order_code IN ('ORD-SAMPLE-001', 'ORD-SAMPLE-002', 'ORD-SAMPLE-004');

-- Trường hợp 2: Sai lệch (gateway nhiều hơn)
INSERT INTO payment_reconciliations (order_id, transaction_id, gateway, gateway_amount, system_amount, discrepancy, status, transaction_date, created_at)
SELECT 
    order_code,
    CONCAT('SEPAY-TXN-', LPAD(id, 6, '0')),
    'SEPAY',
    total + 10000, -- Gateway ghi nhận nhiều hơn 10k
    total,
    10000,
    'MISMATCHED',
    created_at,
    created_at
FROM orders
WHERE order_code = 'ORD-SAMPLE-003';

-- Trường hợp 3: Thiếu trong hệ thống
INSERT INTO payment_reconciliations (order_id, transaction_id, gateway, gateway_amount, system_amount, discrepancy, status, transaction_date, created_at)
VALUES 
('ORD-NOTFOUND-001', 'SEPAY-TXN-999999', 'SEPAY', 500000, 0, 500000, 'MISSING_IN_SYSTEM', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

-- ============================================
-- 5. TẠO NHÀ CUNG CẤP MẪU
-- ============================================
INSERT INTO suppliers (name, tax_code, email, phone, address, payment_term, payment_term_days, active, auto_created, created_at)
VALUES 
('Công ty TNHH ABC', '0123456789', 'abc@example.com', '0901234567', '123 Đường ABC, TP.HCM', 'NET_30', 30, true, false, NOW()),
('Công ty CP XYZ', '9876543210', 'xyz@example.com', '0907654321', '456 Đường XYZ, Hà Nội', 'NET_15', 15, true, false, NOW())
ON DUPLICATE KEY UPDATE name = name; -- Không insert nếu đã tồn tại

-- ============================================
-- 6. TẠO CÔNG NỢ NHÀ CUNG CẤP
-- ============================================
INSERT INTO supplier_payables (supplier_id, purchase_order_id, invoice_number, invoice_date, due_date, total_amount, paid_amount, remaining_amount, status, created_at)
SELECT 
    s.id,
    NULL, -- Không có PO thật
    CONCAT('INV-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(s.id, 3, '0')),
    DATE_SUB(NOW(), INTERVAL 20 DAY),
    DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL s.payment_term_days DAY,
    5000000,
    2000000,
    3000000,
    'PARTIAL',
    DATE_SUB(NOW(), INTERVAL 20 DAY)
FROM suppliers s
WHERE s.tax_code IN ('0123456789', '9876543210')
LIMIT 2;

-- ============================================
-- 7. TẠO THANH TOÁN CHO NCC
-- ============================================
INSERT INTO supplier_payments (payable_id, amount, payment_method, payment_date, reference_number, note, created_at)
SELECT 
    sp.id,
    1000000,
    'BANK_TRANSFER',
    DATE_SUB(NOW(), INTERVAL 10 DAY),
    CONCAT('PAY-', DATE_FORMAT(NOW(), '%Y%m%d'), '-001'),
    'Thanh toán đợt 1',
    DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM supplier_payables sp
LIMIT 1;

INSERT INTO supplier_payments (payable_id, amount, payment_method, payment_date, reference_number, note, created_at)
SELECT 
    sp.id,
    1000000,
    'BANK_TRANSFER',
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    CONCAT('PAY-', DATE_FORMAT(NOW(), '%Y%m%d'), '-002'),
    'Thanh toán đợt 2',
    DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM supplier_payables sp
LIMIT 1;

-- ============================================
-- 8. KIỂM TRA KẾT QUẢ
-- ============================================
SELECT '=== ĐƠN HÀNG MẪU ===' as info;
SELECT order_code, status, payment_status, total, created_at 
FROM orders 
WHERE order_code LIKE 'ORD-SAMPLE-%'
ORDER BY created_at;

SELECT '=== GIAO DỊCH TÀI CHÍNH ===' as info;
SELECT order_id, type, category, amount, description 
FROM financial_transactions 
WHERE order_id LIKE 'ORD-SAMPLE-%'
ORDER BY transaction_date;

SELECT '=== ĐỐI SOÁT THANH TOÁN ===' as info;
SELECT order_id, gateway, gateway_amount, system_amount, discrepancy, status 
FROM payment_reconciliations 
WHERE order_id LIKE 'ORD-SAMPLE-%' OR order_id LIKE 'ORD-NOTFOUND-%'
ORDER BY transaction_date;

SELECT '=== CÔNG NỢ NCC ===' as info;
SELECT sp.invoice_number, s.name, sp.total_amount, sp.paid_amount, sp.remaining_amount, sp.status
FROM supplier_payables sp
JOIN suppliers s ON sp.supplier_id = s.id
ORDER BY sp.created_at DESC;

SELECT '=== THANH TOÁN NCC ===' as info;
SELECT spm.reference_number, sp.invoice_number, spm.amount, spm.payment_date
FROM supplier_payments spm
JOIN supplier_payables sp ON spm.payable_id = sp.id
ORDER BY spm.payment_date DESC;

-- ============================================
-- TỔNG KẾT
-- ============================================
SELECT '=== TỔNG KẾT ===' as info;
SELECT 
    (SELECT COUNT(*) FROM orders WHERE order_code LIKE 'ORD-SAMPLE-%' AND payment_status = 'PAID') as 'Đơn đã thanh toán',
    (SELECT SUM(total) FROM orders WHERE order_code LIKE 'ORD-SAMPLE-%' AND payment_status = 'PAID') as 'Tổng doanh thu',
    (SELECT COUNT(*) FROM financial_transactions WHERE order_id LIKE 'ORD-SAMPLE-%') as 'Giao dịch tài chính',
    (SELECT COUNT(*) FROM payment_reconciliations WHERE order_id LIKE 'ORD-SAMPLE-%' OR order_id LIKE 'ORD-NOTFOUND-%') as 'Bản ghi đối soát',
    (SELECT COUNT(*) FROM supplier_payables) as 'Công nợ NCC',
    (SELECT COUNT(*) FROM supplier_payments) as 'Thanh toán NCC';

-- ============================================
-- HOÀN TẤT
-- ============================================
SELECT '✅ Đã tạo xong dữ liệu mẫu!' as result;
SELECT 'Bây giờ có thể test Module Kế toán với dữ liệu thật!' as next_step;
