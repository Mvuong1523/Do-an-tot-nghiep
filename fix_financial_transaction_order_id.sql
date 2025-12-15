-- Fix: Cho phép order_id NULL trong financial_transaction
-- Vì không phải tất cả giao dịch đều liên quan đến đơn hàng
-- (VD: chi phí marketing, thuê văn phòng, lương nhân viên...)

ALTER TABLE financial_transaction 
MODIFY COLUMN order_id VARCHAR(255) NULL;

-- Kiểm tra kết quả
DESCRIBE financial_transaction;
