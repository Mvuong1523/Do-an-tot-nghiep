-- ============================================
-- SAMPLE DATA FOR web2 DATABASE
-- Run this AFTER backend has created the schema
-- ============================================

USE web2;

-- ============================================
-- 1. INSERT USERS
-- ============================================
-- Password for all users: admin123 (BCrypt hash)
INSERT INTO users (email, password, full_name, phone, role, active, created_at, updated_at)
VALUES 
('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Admin User', '0901234567', 'ADMIN', true, NOW(), NOW()),
('accountant@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Accountant User', '0901234568', 'ACCOUNTANT', true, NOW(), NOW()),
('employee@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Employee User', '0901234569', 'EMPLOYEE', true, NOW(), NOW()),
('user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Test User', '0907654321', 'USER', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = email;

-- ============================================
-- 2. INSERT CATEGORIES
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
-- 3. INSERT SUPPLIERS
-- ============================================
INSERT INTO suppliers (name, tax_code, email, phone, address, payment_term, payment_term_days, active, auto_created, created_at)
VALUES 
('Công ty TNHH Công nghệ ABC', '0123456789', 'abc@tech.com', '0281234567', '456 Đường XYZ, TP.HCM', 'NET_30', 30, true, false, NOW()),
('Công ty CP Điện tử DEF', '9876543210', 'def@electronics.com', '0287654321', '789 Đường MNO, Hà Nội', 'NET_15', 15, true, false, NOW()),
('Nhà phân phối GHI', '1122334455', 'ghi@distributor.com', '0283334444', '321 Đường PQR, Đà Nẵng', 'NET_45', 45, true, false, NOW())
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- 4. INSERT SAMPLE PRODUCTS
-- ============================================
INSERT INTO products (name, description, price, stock_quantity, category_id, active, created_at, updated_at)
SELECT 
    'MacBook Pro 14 M3',
    'MacBook Pro 14 inch với chip M3, RAM 16GB, SSD 512GB',
    45000000,
    10,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c WHERE c.name = 'Laptop' LIMIT 1
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO products (name, description, price, stock_quantity, category_id, active, created_at, updated_at)
SELECT 
    'iPhone 15 Pro Max',
    'iPhone 15 Pro Max 256GB, Titan Tự nhiên',
    33000000,
    25,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c WHERE c.name = 'Điện thoại' LIMIT 1
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO products (name, description, price, stock_quantity, category_id, active, created_at, updated_at)
SELECT 
    'iPad Pro 12.9',
    'iPad Pro 12.9 inch M2, 256GB, WiFi',
    28000000,
    12,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c WHERE c.name = 'Tablet' LIMIT 1
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO products (name, description, price, stock_quantity, category_id, active, created_at, updated_at)
SELECT 
    'AirPods Pro 2',
    'Apple AirPods Pro thế hệ 2, USB-C',
    6000000,
    50,
    c.id,
    true,
    NOW(),
    NOW()
FROM categories c WHERE c.name = 'Phụ kiện' LIMIT 1
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- 5. VERIFICATION
-- ============================================
SELECT '=== USERS ===' as info;
SELECT id, email, full_name, role FROM users;

SELECT '=== CATEGORIES ===' as info;
SELECT id, name, description FROM categories;

SELECT '=== PRODUCTS ===' as info;
SELECT p.id, p.name, p.price, p.stock_quantity, c.name as category 
FROM products p 
JOIN categories c ON p.category_id = c.id;

SELECT '=== SUPPLIERS ===' as info;
SELECT id, name, tax_code, payment_term_days FROM suppliers;

SELECT '✅ Sample data imported successfully!' as result;
SELECT 'Login credentials:' as info;
SELECT 'Admin: admin@example.com / admin123' as admin;
SELECT 'Accountant: accountant@example.com / admin123' as accountant;
SELECT 'Employee: employee@example.com / admin123' as employee;
SELECT 'User: user@example.com / admin123' as user;
