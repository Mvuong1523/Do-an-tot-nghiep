-- Tạo bảng financial_transactions
CREATE TABLE IF NOT EXISTS financial_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DOUBLE NOT NULL,
    order_id BIGINT,
    supplier_id BIGINT,
    description VARCHAR(1000),
    transaction_date DATETIME NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_type (type),
    INDEX idx_category (category),
    INDEX idx_order_id (order_id),
    INDEX idx_supplier_id (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO financial_transactions (transaction_code, type, category, amount, description, transaction_date, created_at, created_by)
VALUES 
('TXN001', 'REVENUE', 'SALES', 5000000, 'Doanh thu bán hàng tháng 12', NOW(), NOW(), 'admin'),
('TXN002', 'EXPENSE', 'SHIPPING', 500000, 'Chi phí vận chuyển', NOW(), NOW(), 'admin'),
('TXN003', 'EXPENSE', 'PAYMENT_FEE', 100000, 'Phí cổng thanh toán', NOW(), NOW(), 'admin');
