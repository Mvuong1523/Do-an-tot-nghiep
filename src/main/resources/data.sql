-- =====================================================
-- Script tạo dữ liệu mẫu cho hệ thống quản lý kho
-- =====================================================

USE web2;

-- 1. Tạo Suppliers (Nhà cung cấp)
INSERT INTO suppliers (name, contact_name, phone, email, address, tax_code, bank_account, payment_term, active, auto_created)
VALUES 
('Công ty TNHH Điện Thoại Việt', 'Nguyễn Văn A', '0901234567', 'contact@dienthoaiviet.com', 
 '123 Đường ABC, Quận 1, TP.HCM', '0123456789', '1234567890', 'Thanh toán trong 30 ngày', 1, 0),
 
('Công ty CP Phân Phối Điện Tử', 'Trần Thị B', '0912345678', 'info@dientu.com', 
 '456 Đường XYZ, Quận 3, TP.HCM', '0987654321', '0987654321', 'Thanh toán ngay', 1, 0),
 
('Công ty TNHH Thương Mại Apple', 'Lê Văn C', '0923456789', 'sales@apple.vn', 
 '789 Đường DEF, Quận 7, TP.HCM', '1122334455', '1122334455', 'Thanh toán trong 15 ngày', 1, 0);

-- 2. Tạo Warehouse Products (Sản phẩm kho)
INSERT INTO warehouse_products (sku, internal_name, description, tech_specs_json, supplier_id, last_import_date)
VALUES 
('IP14-128-BLK', 'iPhone 14 128GB Black', 'iPhone 14 màu đen, bộ nhớ 128GB', 
 '{"ram":"6GB","storage":"128GB","screen":"6.1 inch","chip":"A15 Bionic"}', 1, NOW()),
 
('SS-S23-256-WHT', 'Samsung Galaxy S23 256GB White', 'Samsung Galaxy S23 màu trắng, bộ nhớ 256GB', 
 '{"ram":"8GB","storage":"256GB","screen":"6.1 inch","chip":"Snapdragon 8 Gen 2"}', 1, NOW()),
 
('XM-13-128-BLU', 'Xiaomi 13 128GB Blue', 'Xiaomi 13 màu xanh, bộ nhớ 128GB', 
 '{"ram":"8GB","storage":"128GB","screen":"6.36 inch","chip":"Snapdragon 8 Gen 2"}', 2, NOW()),
 
('IP14-256-WHT', 'iPhone 14 256GB White', 'iPhone 14 màu trắng, bộ nhớ 256GB', 
 '{"ram":"6GB","storage":"256GB","screen":"6.1 inch","chip":"A15 Bionic"}', 3, NOW()),
 
('SS-A54-128-BLK', 'Samsung Galaxy A54 128GB Black', 'Samsung Galaxy A54 màu đen, bộ nhớ 128GB', 
 '{"ram":"8GB","storage":"128GB","screen":"6.4 inch","chip":"Exynos 1380"}', 1, NOW());

-- 3. Tạo Purchase Orders (Phiếu nhập kho)
INSERT INTO purchase_orders (po_code, supplier_tax_code, order_date, received_date, status, created_by, note)
VALUES 
('PO-2024-001', '0123456789', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 
 'RECEIVED', 'admin@webtmdt.com', 'Đơn nhập hàng đầu tiên - iPhone và Samsung'),
 
('PO-2024-002', '0987654321', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 
 'RECEIVED', 'admin@webtmdt.com', 'Đơn nhập hàng Xiaomi'),
 
('PO-2024-003', '1122334455', NOW(), NULL, 
 'CREATED', 'admin@webtmdt.com', 'Đơn hàng đang chờ nhập kho - iPhone trắng'),
 
('PO-2024-004', '0123456789', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 
 'RECEIVED', 'admin@webtmdt.com', 'Đơn nhập Samsung A54');

-- 4. Tạo Purchase Order Items (Chi tiết phiếu nhập)
-- PO-2024-001: iPhone 14 Black và Samsung S23
INSERT INTO purchase_order_items (purchase_order_id, sku, warehouse_product_id, quantity, unit_cost, warranty_months, note)
VALUES 
(1, 'IP14-128-BLK', 1, 5, 18000000, 12, 'iPhone 14 đen - lô 1'),
(1, 'SS-S23-256-WHT', 2, 3, 20000000, 12, 'Samsung S23 trắng');

-- PO-2024-002: Xiaomi 13
INSERT INTO purchase_order_items (purchase_order_id, sku, warehouse_product_id, quantity, unit_cost, warranty_months, note)
VALUES 
(2, 'XM-13-128-BLU', 3, 10, 12000000, 18, 'Xiaomi 13 xanh');

-- PO-2024-003: iPhone 14 White (chưa nhập)
INSERT INTO purchase_order_items (purchase_order_id, sku, warehouse_product_id, quantity, unit_cost, warranty_months, note)
VALUES 
(3, 'IP14-256-WHT', 4, 7, 22000000, 12, 'iPhone 14 trắng 256GB - đợt 2');

-- PO-2024-004: Samsung A54
INSERT INTO purchase_order_items (purchase_order_id, sku, warehouse_product_id, quantity, unit_cost, warranty_months, note)
VALUES 
(4, 'SS-A54-128-BLK', 5, 15, 8500000, 12, 'Samsung A54 đen');

-- 5. Tạo Product Details (Serial/IMEI cho từng sản phẩm)
-- iPhone 14 Black (5 máy)
INSERT INTO product_details (serial_number, import_price, sale_price, import_date, status, warehouse_product_id, purchase_order_item_id, warranty_months, note)
VALUES 
('IP14BLK001', 18000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 1, 1, 12, 'Serial 1'),
('IP14BLK002', 18000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 1, 1, 12, 'Serial 2'),
('IP14BLK003', 18000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 1, 1, 12, 'Serial 3'),
('IP14BLK004', 18000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 1, 1, 12, 'Serial 4'),
('IP14BLK005', 18000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 1, 1, 12, 'Serial 5');

-- Samsung S23 White (3 máy)
INSERT INTO product_details (serial_number, import_price, sale_price, import_date, status, warehouse_product_id, purchase_order_item_id, warranty_months, note)
VALUES 
('SSS23WHT001', 20000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 2, 2, 12, 'Serial 1'),
('SSS23WHT002', 20000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 2, 2, 12, 'Serial 2'),
('SSS23WHT003', 20000000, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 'IN_STOCK', 2, 2, 12, 'Serial 3');

-- Xiaomi 13 Blue (10 máy)
INSERT INTO product_details (serial_number, import_price, sale_price, import_date, status, warehouse_product_id, purchase_order_item_id, warranty_months, note)
VALUES 
('XM13BLU001', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 1'),
('XM13BLU002', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 2'),
('XM13BLU003', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 3'),
('XM13BLU004', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 4'),
('XM13BLU005', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 5'),
('XM13BLU006', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 6'),
('XM13BLU007', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 7'),
('XM13BLU008', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 8'),
('XM13BLU009', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 9'),
('XM13BLU010', 12000000, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'IN_STOCK', 3, 3, 18, 'Serial 10');

-- Samsung A54 Black (15 máy)
INSERT INTO product_details (serial_number, import_price, sale_price, import_date, status, warehouse_product_id, purchase_order_item_id, warranty_months, note)
VALUES 
('SSA54BLK001', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 1'),
('SSA54BLK002', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 2'),
('SSA54BLK003', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 3'),
('SSA54BLK004', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 4'),
('SSA54BLK005', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 5'),
('SSA54BLK006', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 6'),
('SSA54BLK007', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 7'),
('SSA54BLK008', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 8'),
('SSA54BLK009', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 9'),
('SSA54BLK010', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 10'),
('SSA54BLK011', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 11'),
('SSA54BLK012', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 12'),
('SSA54BLK013', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 13'),
('SSA54BLK014', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 14'),
('SSA54BLK015', 8500000, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), 'IN_STOCK', 5, 5, 12, 'Serial 15');

-- =====================================================
-- Tổng kết dữ liệu đã tạo:
-- - 3 nhà cung cấp
-- - 5 sản phẩm kho (warehouse products)
-- - 4 phiếu nhập (3 đã nhập, 1 chờ nhập)
-- - 33 serial/sản phẩm trong kho
-- =====================================================

SELECT '✅ Đã import thành công dữ liệu mẫu phiếu nhập kho!' AS status;
SELECT 
    'Suppliers' AS table_name, 
    COUNT(*) AS total_records 
FROM suppliers
UNION ALL
SELECT 'Warehouse Products', COUNT(*) FROM warehouse_products
UNION ALL
SELECT 'Purchase Orders', COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'Purchase Order Items', COUNT(*) FROM purchase_order_items
UNION ALL
SELECT 'Product Details (Serials)', COUNT(*) FROM product_details;
