-- ===================================
-- DATA MẪU CHO CHỨC NĂNG NHẬP KHO
-- ===================================

-- 1. Thêm nhà cung cấp mẫu
INSERT INTO suppliers (name, contact_person, phone, email, address, tax_code, bank_account, payment_term, active, created_at, updated_at)
VALUES 
('Công ty TNHH Điện Tử ABC', 'Nguyễn Văn A', '0901234567', 'contact@abc.com', '123 Đường ABC, Hà Nội', '0123456789', 'VCB - 1234567890', 'Thanh toán trong 30 ngày', true, NOW(), NOW()),
('Công ty CP Công Nghệ XYZ', 'Trần Thị B', '0912345678', 'info@xyz.com', '456 Đường XYZ, TP.HCM', '9876543210', 'TCB - 0987654321', 'Thanh toán ngay', true, NOW(), NOW());

-- 2. Thêm sản phẩm kho (warehouse_products)
INSERT INTO warehouse_products (sku, internal_name, description, supplier_id, tech_specs_json, created_at, updated_at)
VALUES 
-- Laptop
('WH-LAPTOP-001', 'Laptop Dell Inspiron 15 3000', 'Laptop văn phòng giá rẻ', 1, 
'{"CPU":"Intel Core i3-1115G4","RAM":"8GB DDR4","Storage":"256GB SSD","Display":"15.6 inch FHD","GPU":"Intel UHD Graphics","OS":"Windows 11 Home"}', 
NOW(), NOW()),

('WH-LAPTOP-002', 'Laptop HP Pavilion 14', 'Laptop học tập - làm việc', 1,
'{"CPU":"Intel Core i5-1235U","RAM":"16GB DDR4","Storage":"512GB SSD","Display":"14 inch FHD IPS","GPU":"Intel Iris Xe","OS":"Windows 11 Home"}',
NOW(), NOW()),

('WH-LAPTOP-003', 'Laptop Asus VivoBook 15', 'Laptop đa năng', 2,
'{"CPU":"AMD Ryzen 5 5500U","RAM":"8GB DDR4","Storage":"512GB SSD","Display":"15.6 inch FHD","GPU":"AMD Radeon Graphics","OS":"Windows 11 Home"}',
NOW(), NOW()),

-- Điện thoại
('WH-PHONE-001', 'iPhone 13 128GB', 'Điện thoại Apple', 2,
'{"Display":"6.1 inch Super Retina XDR","Chip":"A15 Bionic","RAM":"4GB","Storage":"128GB","Camera":"12MP + 12MP","Battery":"3240mAh"}',
NOW(), NOW()),

('WH-PHONE-002', 'Samsung Galaxy S23', 'Điện thoại Samsung cao cấp', 1,
'{"Display":"6.1 inch Dynamic AMOLED 2X","Chip":"Snapdragon 8 Gen 2","RAM":"8GB","Storage":"256GB","Camera":"50MP + 12MP + 10MP","Battery":"3900mAh"}',
NOW(), NOW()),

-- Tai nghe
('WH-HEADPHONE-001', 'AirPods Pro 2', 'Tai nghe không dây Apple', 2,
'{"Type":"In-ear","Connectivity":"Bluetooth 5.3","ANC":"Active Noise Cancellation","Battery":"6 hours (ANC on)","Charging":"MagSafe, Lightning"}',
NOW(), NOW()),

('WH-HEADPHONE-002', 'Sony WH-1000XM5', 'Tai nghe chống ồn cao cấp', 1,
'{"Type":"Over-ear","Connectivity":"Bluetooth 5.2","ANC":"Industry-leading ANC","Battery":"30 hours","Charging":"USB-C"}',
NOW(), NOW());

-- 3. Tạo phiếu nhập kho mẫu (purchase_orders)
INSERT INTO purchase_orders (po_code, supplier_id, order_date, status, note, created_by, created_at, updated_at)
VALUES 
('PO-2024-001', 1, NOW(), 'CREATED', 'Đơn nhập laptop Dell và HP', 'admin@example.com', NOW(), NOW()),
('PO-2024-002', 2, NOW(), 'CREATED', 'Đơn nhập điện thoại và tai nghe', 'admin@example.com', NOW(), NOW()),
('PO-2024-003', 1, NOW(), 'CREATED', 'Đơn nhập laptop Asus', 'admin@example.com', NOW(), NOW());

-- 4. Thêm chi tiết phiếu nhập (purchase_order_items)
-- Phiếu PO-2024-001: 5 laptop Dell + 3 laptop HP
INSERT INTO purchase_order_items (purchase_order_id, warehouse_product_id, sku, quantity, unit_cost, warranty_months, note, created_at, updated_at)
SELECT 
    po.id,
    wp.id,
    wp.sku,
    5,
    12500000,
    12,
    'Laptop Dell Inspiron - Hàng mới 100%',
    NOW(),
    NOW()
FROM purchase_orders po, warehouse_products wp
WHERE po.po_code = 'PO-2024-001' AND wp.sku = 'WH-LAPTOP-001';

INSERT INTO purchase_order_items (purchase_order_id, warehouse_product_id, sku, quantity, unit_cost, warranty_months, note, created_at, updated_at)
SELECT 
    po.id,
    wp.id,
    wp.sku,
    3,
    18500000,
    12,
    'Laptop HP Pavilion - Hàng mới 100%',
    NOW(),
    NOW()
FROM purchase_orders po, warehouse_products wp
WHERE po.po_code = 'PO-2024-001' AND wp.sku = 'WH-LAPTOP-002';

-- Phiếu PO-2024-002: 10 iPhone + 5 AirPods
INSERT INTO purchase_order_items (purchase_order_id, warehouse_product_id, sku, quantity, unit_cost, warranty_months, note, created_at, updated_at)
SELECT 
    po.id,
    wp.id,
    wp.sku,
    10,
    19990000,
    12,
    'iPhone 13 128GB - Hàng chính hãng VN/A',
    NOW(),
    NOW()
FROM purchase_orders po, warehouse_products wp
WHERE po.po_code = 'PO-2024-002' AND wp.sku = 'WH-PHONE-001';

INSERT INTO purchase_order_items (purchase_order_id, warehouse_product_id, sku, quantity, unit_cost, warranty_months, note, created_at, updated_at)
SELECT 
    po.id,
    wp.id,
    wp.sku,
    5,
    6490000,
    12,
    'AirPods Pro 2 - Hàng chính hãng Apple',
    NOW(),
    NOW()
FROM purchase_orders po, warehouse_products wp
WHERE po.po_code = 'PO-2024-002' AND wp.sku = 'WH-HEADPHONE-001';

-- Phiếu PO-2024-003: 7 laptop Asus
INSERT INTO purchase_order_items (purchase_order_id, warehouse_product_id, sku, quantity, unit_cost, warranty_months, note, created_at, updated_at)
SELECT 
    po.id,
    wp.id,
    wp.sku,
    7,
    14500000,
    24,
    'Laptop Asus VivoBook - Bảo hành 2 năm',
    NOW(),
    NOW()
FROM purchase_orders po, warehouse_products wp
WHERE po.po_code = 'PO-2024-003' AND wp.sku = 'WH-LAPTOP-003';

-- ===================================
-- HƯỚNG DẪN SỬ DỤNG
-- ===================================
-- 1. Chạy file SQL này để tạo data mẫu
-- 2. Đăng nhập với tài khoản WAREHOUSE (nhân viên kho)
-- 3. Vào menu "Nhập kho" > "Danh sách phiếu nhập"
-- 4. Chọn phiếu có trạng thái "Chờ nhập" (CREATED)
-- 5. Nhập serial cho từng sản phẩm:
--    - Có thể nhập bằng tay
--    - Hoặc click nút camera để quét QR code
-- 6. Sau khi nhập đủ serial, click "Hoàn thiện phiếu nhập"

-- ===================================
-- MẪU SERIAL ĐỂ TEST (nếu nhập tay)
-- ===================================
-- Laptop Dell: DELL-SN-001, DELL-SN-002, DELL-SN-003, DELL-SN-004, DELL-SN-005
-- Laptop HP: HP-SN-001, HP-SN-002, HP-SN-003
-- iPhone 13: IP13-SN-001, IP13-SN-002, IP13-SN-003, IP13-SN-004, IP13-SN-005, IP13-SN-006, IP13-SN-007, IP13-SN-008, IP13-SN-009, IP13-SN-010
-- AirPods Pro: APP2-SN-001, APP2-SN-002, APP2-SN-003, APP2-SN-004, APP2-SN-005
-- Laptop Asus: ASUS-SN-001, ASUS-SN-002, ASUS-SN-003, ASUS-SN-004, ASUS-SN-005, ASUS-SN-006, ASUS-SN-007

-- ===================================
-- KIỂM TRA DATA SAU KHI CHẠY
-- ===================================
-- SELECT * FROM suppliers;
-- SELECT * FROM warehouse_products;
-- SELECT * FROM purchase_orders;
-- SELECT * FROM purchase_order_items;
