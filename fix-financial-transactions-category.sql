-- Fix category column length in financial_transactions table
-- Error: Data truncated for column 'category' at row 1
-- SUPPLIER_PAYMENT is 16 characters, need at least VARCHAR(20)

USE web_tmdt;

-- Check current column definition
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'web_tmdt'
  AND TABLE_NAME = 'financial_transactions'
  AND COLUMN_NAME = 'category';

-- Alter column to support longer enum values
ALTER TABLE financial_transactions 
MODIFY COLUMN category VARCHAR(30) NOT NULL;

-- Verify the change
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'web_tmdt'
  AND TABLE_NAME = 'financial_transactions'
  AND COLUMN_NAME = 'category';

-- Show all enum values for reference
-- SALES (5)
-- SHIPPING (8)
-- PAYMENT_FEE (11)
-- TAX (3)
-- SUPPLIER_PAYMENT (16) <- This is the longest
-- REFUND (6)
-- OTHER_REVENUE (13)
-- OTHER_EXPENSE (13)
