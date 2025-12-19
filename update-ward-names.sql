-- Script để update wardName cho các đơn hàng cũ
-- Chạy script này để fix dữ liệu cũ

-- Kiểm tra các đơn hàng có ward code nhưng chưa có wardName
SELECT id, order_code, ward, ward_name, province, district 
FROM orders 
WHERE ward IS NOT NULL AND (ward_name IS NULL OR ward_name = '');

-- Update wardName dựa trên ward code
-- Lưu ý: Cần map thủ công hoặc gọi GHN API để lấy tên chính xác

-- Ví dụ update cho một số ward code phổ biến:
-- UPDATE orders SET ward_name = 'Phường Dịch Vọng' WHERE ward = '20308';
-- UPDATE orders SET ward_name = 'Phường Nghĩa Đô' WHERE ward = '20311';
-- UPDATE orders SET ward_name = 'Phường Mai Dịch' WHERE ward = '20314';

-- Hoặc có thể set tạm thời wardName = ward để không bị trống
UPDATE orders 
SET ward_name = CONCAT('Phường/Xã (Mã: ', ward, ')')
WHERE ward IS NOT NULL AND (ward_name IS NULL OR ward_name = '');

-- Kiểm tra lại sau khi update
SELECT id, order_code, ward, ward_name, province, district 
FROM orders 
WHERE ward IS NOT NULL
LIMIT 10;
