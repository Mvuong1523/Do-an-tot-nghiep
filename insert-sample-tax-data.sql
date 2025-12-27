-- Tạo dữ liệu mẫu cho tax_reports

-- Xóa dữ liệu cũ (nếu có)
-- DELETE FROM tax_reports;

-- Thêm báo cáo VAT tháng 12/2025
INSERT INTO tax_reports (
    report_code, 
    tax_type, 
    period_start, 
    period_end, 
    taxable_revenue, 
    tax_rate, 
    tax_amount, 
    paid_amount, 
    remaining_tax, 
    status, 
    created_at, 
    created_by
) VALUES 
(
    'VAT-122025',
    'VAT',
    '2025-12-01',
    '2025-12-31',
    100000000,  -- 100 triệu doanh thu
    10,         -- 10% VAT
    10000000,   -- 10 triệu thuế
    0,          -- Chưa nộp
    10000000,   -- Còn nợ 10 triệu
    'DRAFT',
    NOW(),
    'ketoan@gmail.com'
);

-- Thêm báo cáo Thuế TNDN tháng 12/2025
INSERT INTO tax_reports (
    report_code, 
    tax_type, 
    period_start, 
    period_end, 
    taxable_revenue, 
    tax_rate, 
    tax_amount, 
    paid_amount, 
    remaining_tax, 
    status, 
    created_at, 
    created_by
) VALUES 
(
    'TNDN-122025',
    'CORPORATE_TAX',
    '2025-12-01',
    '2025-12-31',
    50000000,   -- 50 triệu lợi nhuận
    20,         -- 20% thuế TNDN
    10000000,   -- 10 triệu thuế
    0,          -- Chưa nộp
    10000000,   -- Còn nợ 10 triệu
    'DRAFT',
    NOW(),
    'ketoan@gmail.com'
);

-- Thêm báo cáo VAT tháng 11/2025 (đã nộp)
INSERT INTO tax_reports (
    report_code, 
    tax_type, 
    period_start, 
    period_end, 
    taxable_revenue, 
    tax_rate, 
    tax_amount, 
    paid_amount, 
    remaining_tax, 
    status, 
    submitted_at,
    paid_at,
    created_at, 
    created_by
) VALUES 
(
    'VAT-112025',
    'VAT',
    '2025-11-01',
    '2025-11-30',
    80000000,   -- 80 triệu doanh thu
    10,         -- 10% VAT
    8000000,    -- 8 triệu thuế
    8000000,    -- Đã nộp đủ
    0,          -- Không còn nợ
    'PAID',
    '2025-12-05 10:00:00',
    '2025-12-10 14:30:00',
    '2025-11-25 09:00:00',
    'ketoan@gmail.com'
);

-- Thêm báo cáo Thuế TNDN tháng 11/2025 (đã gửi, chưa nộp)
INSERT INTO tax_reports (
    report_code, 
    tax_type, 
    period_start, 
    period_end, 
    taxable_revenue, 
    tax_rate, 
    tax_amount, 
    paid_amount, 
    remaining_tax, 
    status, 
    submitted_at,
    created_at, 
    created_by
) VALUES 
(
    'TNDN-112025',
    'CORPORATE_TAX',
    '2025-11-01',
    '2025-11-30',
    45000000,   -- 45 triệu lợi nhuận
    20,         -- 20% thuế TNDN
    9000000,    -- 9 triệu thuế
    0,          -- Chưa nộp
    9000000,    -- Còn nợ 9 triệu
    'SUBMITTED',
    '2025-12-05 11:00:00',
    '2025-11-25 10:00:00',
    'ketoan@gmail.com'
);

-- Kiểm tra dữ liệu đã thêm
SELECT 
    report_code,
    tax_type,
    taxable_revenue,
    tax_amount,
    remaining_tax,
    status
FROM tax_reports
ORDER BY created_at DESC;

-- Kiểm tra tổng hợp
SELECT 
    tax_type,
    COUNT(*) as total_reports,
    SUM(tax_amount) as total_tax,
    SUM(remaining_tax) as total_remaining
FROM tax_reports
GROUP BY tax_type;
