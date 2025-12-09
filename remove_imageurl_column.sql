-- Migration: Remove imageUrl column from products table
-- Date: 2025-12-09
-- Description: Remove deprecated imageUrl column as we now use product_images table

ALTER TABLE products DROP COLUMN imageUrl;
