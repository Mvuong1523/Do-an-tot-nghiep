-- ===== RESET MẬT KHẨU NHANH =====
-- Mật khẩu mới cho tất cả: "123456"

-- 1. Reset cho tất cả customers
UPDATE customers 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- 2. Reset cho tất cả employees  
UPDATE employees 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- 3. Reset cho admin user (nếu có bảng users)
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE role = 'ADMIN';

-- ===== SAU KHI CHẠY SCRIPT =====
-- Tất cả tài khoản có mật khẩu: 123456
-- Đăng nhập và đổi mật khẩu ngay!

-- ===== HOẶC RESET CHO 1 TÀI KHOẢN CỤ THỂ =====
-- Thay 'your-email@example.com' bằng email của bạn:

-- UPDATE customers 
-- SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
-- WHERE email = 'your-email@example.com';

-- ===== KIỂM TRA =====
SELECT email, password FROM customers LIMIT 5;
SELECT email, password FROM employees LIMIT 5;
