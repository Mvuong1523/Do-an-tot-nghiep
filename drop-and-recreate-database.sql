-- ============================================
-- DROP AND RECREATE DATABASE web2
-- This will completely reset the database
-- ============================================

-- Drop the database if it exists
DROP DATABASE IF EXISTS web2;

-- Create fresh database
CREATE DATABASE web2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Select the database
USE web2;

-- Database is now empty and ready for Hibernate to create tables
SELECT 'Database web2 has been dropped and recreated successfully!' as result;
SELECT 'Now restart the backend with ddl-auto=create to create all tables' as next_step;
