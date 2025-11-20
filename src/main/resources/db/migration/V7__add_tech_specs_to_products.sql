-- Thêm cột tech_specs_json vào bảng products
ALTER TABLE products ADD COLUMN tech_specs_json TEXT;

-- Copy thông số từ warehouse_products sang products (cho các sản phẩm đã đăng bán)
UPDATE products p
INNER JOIN warehouse_products wp ON p.warehouse_product_id = wp.id
SET p.tech_specs_json = wp.tech_specs_json
WHERE wp.tech_specs_json IS NOT NULL;
