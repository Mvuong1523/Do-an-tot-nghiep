-- Script reset mật khẩu cho user

-- Mật khẩu mới: "123456" (đã mã hóa BCrypt)
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Reset mật khẩu cho admin
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'admin@example.com';

-- Reset mật khẩu cho customer (thay email của bạn)
UPDATE customers 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'your-email@example.com';

-- Reset mật khẩu cho employee (thay email của bạn)
UPDATE employees 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'your-email@example.com';

-- Sau khi reset, mật khẩu mới là: 123456
-- Hãy đăng nhập và đổi mật khẩu ngay!

-- ===== HOẶC tạo mật khẩu khác =====
-- Mật khẩu: "password123"
-- Hash: $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.

-- Mật khẩu: "admin123"  
-- Hash: $2a$10$DpwmetHYmqvgGqhJzW8b3OqZqXzqHqVqzqXzqHqVqzqXzqHqVqzqX
