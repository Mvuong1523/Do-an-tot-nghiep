-- ============================================
-- SCRIPT THÊM SẢN PHẨM MẪU
-- Chỉ insert sản phẩm, không tạo user/category
-- ============================================

USE web_tmdt;

-- ============================================
-- THÊM SẢN PHẨM MẪU
-- ============================================
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, active, created_at, updated_at)
VALUES 
-- Laptop
('MacBook Pro 14 M3', 'MacBook Pro 14 inch với chip M3, RAM 16GB, SSD 512GB', 45000000, 10, 1, 
 'https://cdn.tgdd.vn/Products/Images/44/309016/macbook-pro-14-m3-2023-thumb-600x600.jpg', true, NOW(), NOW()),

('Dell XPS 13', 'Dell XPS 13 inch, Intel Core i7, RAM 16GB, SSD 512GB', 32000000, 15, 1,
 'https://cdn.tgdd.vn/Products/Images/44/235981/dell-xps-13-9315-i7-71003600-thumb-600x600.jpg', true, NOW(), NOW()),

('Lenovo ThinkPad X1', 'Lenovo ThinkPad X1 Carbon Gen 11, Intel Core i7, RAM 16GB', 35000000, 8, 1,
 'https://cdn.tgdd.vn/Products/Images/44/309854/lenovo-thinkpad-x1-carbon-gen-11-i7-thumb-600x600.jpg', true, NOW(), NOW()),

('HP Pavilion 15', 'HP Pavilion 15 inch, AMD Ryzen 5, RAM 8GB, SSD 256GB', 18000000, 20, 1,
 'https://cdn.tgdd.vn/Products/Images/44/307203/hp-pavilion-15-eg2081tu-i5-thumb-600x600.jpg', true, NOW(), NOW()),

('ASUS ROG Strix G15', 'ASUS ROG Gaming Laptop, RTX 4060, Intel Core i7, RAM 16GB', 38000000, 5, 1,
 'https://cdn.tgdd.vn/Products/Images/44/309123/asus-rog-strix-g15-g513-r7-thumb-600x600.jpg', true, NOW(), NOW()),

-- Điện thoại
('iPhone 15 Pro Max', 'iPhone 15 Pro Max 256GB, Titan Tự nhiên', 33000000, 25, 2,
 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-thumbnew-600x600.jpg', true, NOW(), NOW()),

('Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra 256GB, Màu Đen', 28000000, 30, 2,
 'https://cdn.tgdd.vn/Products/Images/42/307174/samsung-galaxy-s24-ultra-grey-thumbnew-600x600.jpg', true, NOW(), NOW()),

('Xiaomi 14 Pro', 'Xiaomi 14 Pro 256GB, Màu Trắng', 18000000, 40, 2,
 'https://cdn.tgdd.vn/Products/Images/42/319735/xiaomi-14-white-thumbnew-600x600.jpg', true, NOW(), NOW()),

('OPPO Find X7', 'OPPO Find X7 256GB, Màu Xanh', 15000000, 35, 2,
 'https://cdn.tgdd.vn/Products/Images/42/322096/oppo-find-x7-xanh-thumbnew-600x600.jpg', true, NOW(), NOW()),

('Google Pixel 8 Pro', 'Google Pixel 8 Pro 256GB, Màu Đen', 22000000, 15, 2,
 'https://cdn.tgdd.vn/Products/Images/42/309831/google-pixel-8-pro-black-thumbnew-600x600.jpg', true, NOW(), NOW()),

-- Tablet
('iPad Pro 12.9', 'iPad Pro 12.9 inch M2, 256GB, WiFi', 28000000, 12, 3,
 'https://cdn.tgdd.vn/Products/Images/522/289699/ipad-pro-129-inch-wifi-256gb-2022-thumb-600x600.jpg', true, NOW(), NOW()),

('Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 256GB, WiFi', 18000000, 18, 3,
 'https://cdn.tgdd.vn/Products/Images/522/309016/samsung-galaxy-tab-s9-thumb-600x600.jpg', true, NOW(), NOW()),

('iPad Air', 'iPad Air 10.9 inch M1, 128GB, WiFi', 15000000, 20, 3,
 'https://cdn.tgdd.vn/Products/Images/522/235995/ipad-air-5-wifi-purple-thumb-600x600.jpg', true, NOW(), NOW()),

-- Phụ kiện
('AirPods Pro 2', 'Apple AirPods Pro thế hệ 2, USB-C', 6000000, 50, 4,
 'https://cdn.tgdd.vn/Products/Images/54/325533/airpods-pro-2-usb-c-thumb-600x600.jpg', true, NOW(), NOW()),

('Magic Mouse', 'Apple Magic Mouse 2, Màu Trắng', 2000000, 30, 4,
 'https://cdn.tgdd.vn/Products/Images/86/329890/apple-magic-mouse-2-trang-thumb-600x600.jpg', true, NOW(), NOW()),

('Logitech MX Master 3S', 'Chuột không dây Logitech MX Master 3S', 2500000, 40, 4,
 'https://cdn.tgdd.vn/Products/Images/86/285308/logitech-mx-master-3s-den-thumb-600x600.jpg', true, NOW(), NOW()),

('Samsung T7 SSD 1TB', 'Ổ cứng SSD di động Samsung T7 1TB', 3000000, 25, 4,
 'https://cdn.tgdd.vn/Products/Images/67/235533/samsung-t7-1tb-xam-thumb-600x600.jpg', true, NOW(), NOW()),

('Anker PowerBank 20000mAh', 'Pin sạc dự phòng Anker 20000mAh, 65W', 1500000, 60, 4,
 'https://cdn.tgdd.vn/Products/Images/57/316632/anker-powercore-20000mah-a1287-thumb-600x600.jpg', true, NOW(), NOW()),

-- PC
('iMac 24 M3', 'iMac 24 inch M3, RAM 16GB, SSD 512GB', 38000000, 8, 5,
 'https://cdn.tgdd.vn/Products/Images/5697/329890/imac-24-m3-2023-xanh-thumb-600x600.jpg', true, NOW(), NOW()),

('Dell Inspiron Desktop', 'Dell Inspiron Desktop, Intel Core i5, RAM 16GB', 15000000, 12, 5,
 'https://cdn.tgdd.vn/Products/Images/5697/235981/dell-inspiron-3910-i5-thumb-600x600.jpg', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- KIỂM TRA KẾT QUẢ
-- ============================================
SELECT '=== SẢN PHẨM ĐÃ THÊM ===' as info;
SELECT p.id, p.name, p.price, p.stock_quantity, c.name as category 
FROM products p 
LEFT JOIN categories c ON p.category_id = c.id
ORDER BY p.id DESC
LIMIT 20;

SELECT '=== TỔNG KẾT ===' as info;
SELECT 
    COUNT(*) as 'Tổng số sản phẩm',
    SUM(stock_quantity) as 'Tổng số lượng tồn kho',
    MIN(price) as 'Giá thấp nhất',
    MAX(price) as 'Giá cao nhất',
    AVG(price) as 'Giá trung bình'
FROM products;

SELECT '✅ Đã thêm 20 sản phẩm mẫu thành công!' as result;
