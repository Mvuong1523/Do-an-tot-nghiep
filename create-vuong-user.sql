-- Tạo user vuong@gmail.com
-- Mật khẩu mặc định: "password123" (đã được mã hóa bằng BCrypt)
-- Bạn cần thay đổi mật khẩu sau khi đăng nhập

-- 1. Tạo user với role CUSTOMER
INSERT INTO users (email, password, role, status) 
VALUES ('vuong@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'CUSTOMER', 'ACTIVE');

-- Lấy ID của user vừa tạo
SET @user_id = LAST_INSERT_ID();

-- 2. Tạo customer profile cho user này
INSERT INTO customers (user_id, full_name, phone, address) 
VALUES (@user_id, 'Vuong', '0123456789', 'Hà Nội');

-- Kiểm tra kết quả
SELECT u.id, u.email, u.role, u.status, c.full_name, c.phone 
FROM users u 
LEFT JOIN customers c ON c.user_id = u.id 
WHERE u.email = 'vuong@gmail.com';

-- ============================================
-- HOẶC nếu muốn tạo user với role EMPLOYEE
-- ============================================

-- Xóa user cũ nếu đã tạo ở trên
-- DELETE FROM users WHERE email = 'vuong@gmail.com';

-- Tạo user với role EMPLOYEE
-- INSERT INTO users (email, password, role, status) 
-- VALUES ('vuong@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'EMPLOYEE', 'ACTIVE');

-- SET @user_id = LAST_INSERT_ID();

-- Tạo employee profile (cần có position)
-- INSERT INTO employees (user_id, full_name, phone, position, hire_date) 
-- VALUES (@user_id, 'Vuong', '0123456789', 'SALES', CURDATE());

-- ============================================
-- HOẶC nếu muốn tạo user với role ADMIN
-- ============================================

-- DELETE FROM users WHERE email = 'vuong@gmail.com';

-- INSERT INTO users (email, password, role, status) 
-- VALUES ('vuong@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'ADMIN', 'ACTIVE');

-- SET @user_id = LAST_INSERT_ID();

-- INSERT INTO employees (user_id, full_name, phone, position, hire_date) 
-- VALUES (@user_id, 'Vuong Admin', '0123456789', 'MANAGER', CURDATE());
