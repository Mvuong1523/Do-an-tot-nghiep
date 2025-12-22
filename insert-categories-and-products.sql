-- ============================================
-- SCRIPT THÊM DANH MỤC VÀ SẢN PHẨM
-- Chạy toàn bộ script này trong phpMyAdmin
-- ============================================

USE web3;

-- ============================================
-- BƯỚC 1: XÓA DỮ LIỆU CŨ (NẾU CẦN)
-- ============================================
-- Uncomment dòng dưới nếu muốn xóa sản phẩm cũ
-- DELETE FROM products;
-- DELETE FROM categories;

-- ============================================
-- BƯỚC 2: TẠO DANH MỤC
-- ============================================
INSERT INTO categories (name, description, active)
VALUES 
('Laptop', 'Máy tính xách tay các loại', 1),
('Điện thoại', 'Smartphone và phụ kiện', 1),
('Tablet', 'Máy tính bảng', 1),
('Phụ kiện', 'Phụ kiện công nghệ', 1),
('PC', 'Máy tính để bàn', 1);

-- ============================================
-- BƯỚC 3: THÊM SẢN PHẨM (SỬ DỤNG ID CỐ ĐỊNH)
-- Giả sử categories có ID từ 1-5
-- ============================================
INSERT INTO products (name, description, price, stock_quantity, category_id, active)
VALUES 
-- Laptop (category_id = 1)
('MacBook Pro 14 M3', 'MacBook Pro 14 inch với chip M3, RAM 16GB, SSD 512GB', 45000000, 10, 1, 1),
('Dell XPS 13', 'Dell XPS 13 inch, Intel Core i7, RAM 16GB, SSD 512GB', 32000000, 15, 1, 1),
('Lenovo ThinkPad X1', 'Lenovo ThinkPad X1 Carbon Gen 11, Intel Core i7, RAM 16GB', 35000000, 8, 1, 1),
('HP Pavilion 15', 'HP Pavilion 15 inch, AMD Ryzen 5, RAM 8GB, SSD 256GB', 18000000, 20, 1, 1),
('ASUS ROG Strix G15', 'ASUS ROG Gaming Laptop, RTX 4060, Intel Core i7, RAM 16GB', 38000000, 5, 1, 1),

-- Điện thoại (category_id = 2)
('iPhone 15 Pro Max', 'iPhone 15 Pro Max 256GB, Titan Tự nhiên', 33000000, 25, 2, 1),
('Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra 256GB, Màu Đen', 28000000, 30, 2, 1),
('Xiaomi 14 Pro', 'Xiaomi 14 Pro 256GB, Màu Trắng', 18000000, 40, 2, 1),
('OPPO Find X7', 'OPPO Find X7 256GB, Màu Xanh', 15000000, 35, 2, 1),
('Google Pixel 8 Pro', 'Google Pixel 8 Pro 256GB, Màu Đen', 22000000, 15, 2, 1),

-- Tablet (category_id = 3)
('iPad Pro 12.9', 'iPad Pro 12.9 inch M2, 256GB, WiFi', 28000000, 12, 3, 1),
('Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 256GB, WiFi', 18000000, 18, 3, 1),
('iPad Air', 'iPad Air 10.9 inch M1, 128GB, WiFi', 15000000, 20, 3, 1),

-- Phụ kiện (category_id = 4)
('AirPods Pro 2', 'Apple AirPods Pro thế hệ 2, USB-C', 6000000, 50, 4, 1),
('Magic Mouse', 'Apple Magic Mouse 2, Màu Trắng', 2000000, 30, 4, 1),
('Logitech MX Master 3S', 'Chuột không dây Logitech MX Master 3S', 2500000, 40, 4, 1),
('Samsung T7 SSD 1TB', 'Ổ cứng SSD di động Samsung T7 1TB', 3000000, 25, 4, 1),
('Anker PowerBank 20000mAh', 'Pin sạc dự phòng Anker 20000mAh, 65W', 1500000, 60, 4, 1),

-- PC (category_id = 5)
('iMac 24 M3', 'iMac 24 inch M3, RAM 16GB, SSD 512GB', 38000000, 8, 5, 1),
('Dell Inspiron Desktop', 'Dell Inspiron Desktop, Intel Core i5, RAM 16GB', 15000000, 12, 5, 1);

-- ============================================
-- BƯỚC 4: KIỂM TRA KẾT QUẢ
-- ============================================
SELECT '=== DANH MỤC ===' as info;
SELECT * FROM categories;

SELECT '=== SẢN PHẨM VỪA THÊM ===' as info;
SELECT p.id, p.name, p.price, p.stock_quantity, c.name as category 
FROM products p 
LEFT JOIN categories c ON p.category_id = c.id
ORDER BY p.id DESC
LIMIT 20;

SELECT '=== TỔNG KẾT ===' as info;
SELECT 
    (SELECT COUNT(*) FROM categories) as 'Tổng danh mục',
    (SELECT COUNT(*) FROM products) as 'Tổng sản phẩm',
    (SELECT SUM(stock_quantity) FROM products) as 'Tổng tồn kho';

-- ============================================
-- ✅ HOÀN TẤT
-- ============================================
-- Đã thêm:
-- - 5 danh mục (Laptop, Điện thoại, Tablet, Phụ kiện, PC)
-- - 20 sản phẩm với giá và số lượng tồn kho
-- 
-- CÁCH SỬ DỤNG:
-- 1. Mở phpMyAdmin
-- 2. Chọn database 'web3'
-- 3. Vào tab SQL
-- 4. Copy toàn bộ script này và paste vào
-- 5. Click "Go" để chạy
-- ============================================
