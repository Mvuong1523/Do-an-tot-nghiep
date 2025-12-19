-- Kiểm tra các đơn hàng có shippingAddress chứa mã ward thay vì tên
-- (Mã ward thường là số hoặc có format đặc biệt)
SELECT 
    id,
    order_code,
    status,
    shipping_address,
    address,
    ward,
    ward_name,
    district,
    province,
    created_at
FROM orders
WHERE shipping_address IS NOT NULL
ORDER BY created_at DESC
LIMIT 20;

-- Kiểm tra các đơn hàng có ward code trong shippingAddress
-- (Tìm các địa chỉ có pattern số giữa dấu phẩy)
SELECT 
    id,
    order_code,
    shipping_address,
    ward,
    ward_name
FROM orders
WHERE shipping_address REGEXP '[0-9]{5}'  -- Ward code thường là 5 chữ số
  AND (ward_name IS NULL OR ward_name = '')
ORDER BY created_at DESC;

-- So sánh shippingAddress cũ và mới (nếu có wardName)
SELECT 
    order_code,
    shipping_address as old_address,
    CONCAT(address, ', ', 
           COALESCE(ward_name, ward), ', ', 
           district, ', ', 
           province) as new_address,
    ward,
    ward_name
FROM orders
WHERE ward IS NOT NULL
ORDER BY created_at DESC
LIMIT 10;

-- Đếm số đơn hàng cần rebuild address
SELECT 
    COUNT(*) as total_need_rebuild
FROM orders
WHERE ward IS NOT NULL 
  AND (ward_name IS NULL OR ward_name = '')
  AND shipping_address LIKE CONCAT('%', ward, '%');
