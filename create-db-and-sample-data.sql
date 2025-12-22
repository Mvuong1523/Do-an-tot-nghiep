-- ============================================
-- SCRIPT TẠO DATABASE VÀ DỮ LIỆU MẪU
-- Để test hệ thống E-commerce
-- ============================================

-- 1. TẠO DATABASE (nếu chưa có)
CREATE DATABASE IF NOT EXISTS web_tmdt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE web_tmdt;

-- ============================================
-- 2. TẠO USER ADMIN
-- ============================================
-- Password: admin123 (đã hash bằng BCrypt)
INSERT INTO users (email, password, full_name, phone, role, active, created_at, updated_at)
VALUES 
('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Admin User', '0901234567', 'ADMIN', true, NOW(), NOW()),
('user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Test User', '0907654321', 'USER', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = email;

-- ============================================
-- 3. TẠO DANH MỤC SẢN PHẨM
-- ============================================
INSERT INTO categories (name, description, active, created_at, updated_at)
VALUES 
('Laptop', 'Máy tính xách tay các loại', true, NOW(), NOW()),
('Điện thoại', 'Smartphone và phụ kiện', true, NOW(), NOW()),
('Tablet', 'Máy tính bảng', true, NOW(), NOW()),
('Phụ kiện', 'Phụ kiện công nghệ', true, NOW(), NOW()),
('PC', 'Máy tính để bàn', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- 4. TẠO SẢN PHẨM MẪU
-- ============================================
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, active, created_at, updated_at)
VALUES 
-- Laptop
('MacBook Pro 14 M3', 'MacBook Pro 14 inch với chip M3, RAM 16GB, SSD 512GB', 45000000, 10, 
 (SELECT id FROM categories WHERE name = 'Laptop' LIMIT 1), 
 'https://res.cloudinary.com/demo/image/upload/macbook-pro.jpg', true, NOW(), NOW()),

('Dell XPS 13', 'Dell XPS 13 inch, Intel Core i7, RAM 16GB, SSD 512GB', 32000000, 15,
 (SELECT id FROM categories WHERE name = 'Laptop' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/dell-xps.jpg', true, NOW(), NOW()),

('Lenovo ThinkPad X1', 'Lenovo ThinkPad X1 Carbon Gen 11, Intel Core i7, RAM 16GB', 35000000, 8,
 (SELECT id FROM categories WHERE name = 'Laptop' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/thinkpad.jpg', true, NOW(), NOW()),

('HP Pavilion 15', 'HP Pavilion 15 inch, AMD Ryzen 5, RAM 8GB, SSD 256GB', 18000000, 20,
 (SELECT id FROM categories WHERE name = 'Laptop' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/hp-pavilion.jpg', true, NOW(), NOW()),

('ASUS ROG Strix G15', 'ASUS ROG Gaming Laptop, RTX 4060, Intel Core i7, RAM 16GB', 38000000, 5,
 (SELECT id FROM categories WHERE name = 'Laptop' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/asus-rog.jpg', true, NOW(), NOW()),

-- Điện thoại
('iPhone 15 Pro Max', 'iPhone 15 Pro Max 256GB, Titan Tự nhiên', 33000000, 25,
 (SELECT id FROM categories WHERE name = 'Điện thoại' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/iphone-15-pro.jpg', true, NOW(), NOW()),

('Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra 256GB, Màu Đen', 28000000, 30,
 (SELECT id FROM categories WHERE name = 'Điện thoại' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/samsung-s24.jpg', true, NOW(), NOW()),

('Xiaomi 14 Pro', 'Xiaomi 14 Pro 256GB, Màu Trắng', 18000000, 40,
 (SELECT id FROM categories WHERE name = 'Điện thoại' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/xiaomi-14.jpg', true, NOW(), NOW()),

('OPPO Find X7', 'OPPO Find X7 256GB, Màu Xanh', 15000000, 35,
 (SELECT id FROM categories WHERE name = 'Điện thoại' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/oppo-find.jpg', true, NOW(), NOW()),

('Google Pixel 8 Pro', 'Google Pixel 8 Pro 256GB, Màu Đen', 22000000, 15,
 (SELECT id FROM categories WHERE name = 'Điện thoại' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/pixel-8.jpg', true, NOW(), NOW()),

-- Tablet
('iPad Pro 12.9', 'iPad Pro 12.9 inch M2, 256GB, WiFi', 28000000, 12,
 (SELECT id FROM categories WHERE name = 'Tablet' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/ipad-pro.jpg', true, NOW(), NOW()),

('Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 256GB, WiFi', 18000000, 18,
 (SELECT id FROM categories WHERE name = 'Tablet' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/galaxy-tab.jpg', true, NOW(), NOW()),

('iPad Air', 'iPad Air 10.9 inch M1, 128GB, WiFi', 15000000, 20,
 (SELECT id FROM categories WHERE name = 'Tablet' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/ipad-air.jpg', true, NOW(), NOW()),

-- Phụ kiện
('AirPods Pro 2', 'Apple AirPods Pro thế hệ 2, USB-C', 6000000, 50,
 (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/airpods-pro.jpg', true, NOW(), NOW()),

('Magic Mouse', 'Apple Magic Mouse 2, Màu Trắng', 2000000, 30,
 (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/magic-mouse.jpg', true, NOW(), NOW()),

('Logitech MX Master 3S', 'Chuột không dây Logitech MX Master 3S', 2500000, 40,
 (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/mx-master.jpg', true, NOW(), NOW()),

('Samsung T7 SSD 1TB', 'Ổ cứng SSD di động Samsung T7 1TB', 3000000, 25,
 (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/samsung-t7.jpg', true, NOW(), NOW()),

('Anker PowerBank 20000mAh', 'Pin sạc dự phòng Anker 20000mAh, 65W', 1500000, 60,
 (SELECT id FROM categories WHERE name = 'Phụ kiện' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/anker-powerbank.jpg', true, NOW(), NOW()),

-- PC
('iMac 24 M3', 'iMac 24 inch M3, RAM 16GB, SSD 512GB', 38000000, 8,
 (SELECT id FROM categories WHERE name = 'PC' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/imac-24.jpg', true, NOW(), NOW()),

('Dell Inspiron Desktop', 'Dell Inspiron Desktop, Intel Core i5, RAM 16GB', 15000000, 12,
 (SELECT id FROM categories WHERE name = 'PC' LIMIT 1),
 'https://res.cloudinary.com/demo/image/upload/dell-desktop.jpg', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- 5. TẠO ĐỊA CHỈ MẪU CHO USER
-- ============================================
INSERT INTO addresses (user_id, full_name, phone, address_line, ward, district, city, is_default, created_at, updated_at)
SELECT 
    u.id,
    'Nguyễn Văn A',
    '0901234567',
    '123 Đường ABC',
    'Phường 1',
    'Quận 1',
    'TP. Hồ Chí Minh',
    true,
    NOW(),
    NOW()
FROM users u
WHERE u.email = 'user@example.com'
ON DUPLICATE KEY UPDATE full_name = full_name;

-- ============================================
-- 6. TẠO NHÀ CUNG CẤP MẪU
-- ============================================
INSERT INTO suppliers (name, tax_code, email, phone, address, payment_term, payment_term_days, active, auto_created, created_at)
VALUES 
('Công ty TNHH Công nghệ ABC', '0123456789', 'abc@tech.com', '0281234567', '456 Đường XYZ, TP.HCM', 'NET_30', 30, true, false, NOW()),
('Công ty CP Điện tử DEF', '9876543210', 'def@electronics.com', '0287654321', '789 Đường MNO, Hà Nội', 'NET_15', 15, true, false, NOW()),
('Nhà phân phối GHI', '1122334455', 'ghi@distributor.com', '0283334444', '321 Đường PQR, Đà Nẵng', 'NET_45', 45, true, false, NOW())
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- 7. KIỂM TRA KẾT QUẢ
-- ============================================
SELECT '=== USERS ===' as info;
SELECT id, email, full_name, role FROM users;

SELECT '=== CATEGORIES ===' as info;
SELECT id, name, description FROM categories;

SELECT '=== PRODUCTS ===' as info;
SELECT p.id, p.name, p.price, p.stock_quantity, c.name as category 
FROM products p 
JOIN categories c ON p.category_id = c.id
ORDER BY c.name, p.name;

SELECT '=== SUPPLIERS ===' as info;
SELECT id, name, tax_code, payment_term_days FROM suppliers;

SELECT '=== ADDRESSES ===' as info;
SELECT a.id, a.full_name, a.phone, a.city, u.email 
FROM addresses a 
JOIN users u ON a.user_id = u.id;

-- ============================================
-- TỔNG KẾT
-- ============================================
SELECT '=== SUMMARY ===' as info;
SELECT 
    (SELECT COUNT(*) FROM users) as 'Total Users',
    (SELECT COUNT(*) FROM categories) as 'Total Categories',
    (SELECT COUNT(*) FROM products) as 'Total Products',
    (SELECT COUNT(*) FROM suppliers) as 'Total Suppliers',
    (SELECT COUNT(*) FROM addresses) as 'Total Addresses';

SELECT '✅ Database và dữ liệu mẫu đã được tạo thành công!' as result;
SELECT 'Bạn có thể login với:' as info;
SELECT 'Email: admin@example.com' as admin_login;
SELECT 'Password: admin123' as admin_password;
SELECT 'Email: user@example.com' as user_login;
SELECT 'Password: admin123' as user_password;
