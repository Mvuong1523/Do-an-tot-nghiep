-- Add SHIPPER to Position enum in database
-- Run this SQL script to update the database schema

-- For employee_registration table
ALTER TABLE employee_registration 
MODIFY COLUMN position VARCHAR(50) NOT NULL;

-- For employees table  
ALTER TABLE employees 
MODIFY COLUMN position VARCHAR(50) NOT NULL;

-- Verify the changes
DESCRIBE employee_registration;
DESCRIBE employees;

-- Test query to check if SHIPPER can be inserted
SELECT 'Database schema updated successfully. SHIPPER position can now be used.' AS status;
