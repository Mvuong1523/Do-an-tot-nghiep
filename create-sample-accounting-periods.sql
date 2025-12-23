-- Tạo dữ liệu mẫu cho bảng accounting_periods
USE web3;

-- Xóa dữ liệu cũ nếu có
DELETE FROM accounting_periods;

-- Reset auto increment
ALTER TABLE accounting_periods AUTO_INCREMENT = 1;

-- Tạo kỳ kế toán mẫu
INSERT INTO accounting_periods (name, start_date, end_date, status, total_revenue, total_expense, net_profit, discrepancy_rate, created_at, created_by) VALUES
('Kỳ tháng 11/2024', '2024-11-01', '2024-11-30', 'CLOSED', 150000000, 80000000, 70000000, 2.5, NOW(), 'admin@webtmdt.com'),
('Kỳ tháng 12/2024', '2024-12-01', '2024-12-31', 'OPEN', 200000000, 120000000, 80000000, 5.2, NOW(), 'admin@webtmdt.com'),
('Kỳ Quý 1/2025', '2025-01-01', '2025-03-31', 'OPEN', 0, 0, 0, 0, NOW(), 'admin@webtmdt.com');

-- Kiểm tra dữ liệu
SELECT * FROM accounting_periods ORDER BY start_date DESC;

-- Hiển thị thông tin
SELECT 
    id,
    name,
    DATE_FORMAT(start_date, '%d/%m/%Y') as 'Từ ngày',
    DATE_FORMAT(end_date, '%d/%m/%Y') as 'Đến ngày',
    status as 'Trạng thái',
    FORMAT(total_revenue, 0) as 'Doanh thu',
    FORMAT(total_expense, 0) as 'Chi phí',
    FORMAT(net_profit, 0) as 'Lợi nhuận',
    CONCAT(discrepancy_rate, '%') as 'Sai số',
    closed_by as 'Người chốt'
FROM accounting_periods
ORDER BY start_date DESC;
