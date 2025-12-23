-- Tạo bảng accounting_periods
CREATE TABLE IF NOT EXISTS accounting_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    total_revenue DOUBLE,
    total_expense DOUBLE,
    net_profit DOUBLE,
    discrepancy_rate DOUBLE,
    closed_at DATETIME,
    closed_by VARCHAR(255),
    created_at DATETIME,
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date),
    INDEX idx_status (status),
    UNIQUE KEY uk_period (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample periods
INSERT INTO accounting_periods (name, start_date, end_date, status, total_revenue, total_expense, net_profit, discrepancy_rate, created_at)
VALUES 
('Tháng 12/2024', '2024-12-01', '2024-12-31', 'OPEN', 50000000, 10000000, 40000000, 2.5, NOW()),
('Tháng 11/2024', '2024-11-01', '2024-11-30', 'CLOSED', 45000000, 9000000, 36000000, 1.8, NOW());
