-- ===================================================================
-- Migration: Tạo bảng product_images
-- Chạy script này trong MySQL/MariaDB
-- ===================================================================

-- Tạo bảng product_images
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Tạo indexes để tăng performance
CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_display_order ON product_images(display_order);
CREATE INDEX idx_product_images_is_primary ON product_images(is_primary);

-- Migrate dữ liệu cũ (nếu có ảnh trong products.image_url)
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT 
    id as product_id,
    image_url,
    0 as display_order,
    TRUE as is_primary,
    NOW() as created_at
FROM products
WHERE image_url IS NOT NULL 
  AND image_url != ''
  AND NOT EXISTS (
      SELECT 1 FROM product_images pi WHERE pi.product_id = products.id
  );

-- Kiểm tra kết quả
SELECT 
    p.id as product_id,
    p.name as product_name,
    COUNT(pi.id) as image_count
FROM products p
LEFT JOIN product_images pi ON p.id = pi.product_id
GROUP BY p.id, p.name
ORDER BY image_count DESC
LIMIT 10;

-- ===================================================================
-- Hoàn thành! Bảng product_images đã được tạo
-- ===================================================================
