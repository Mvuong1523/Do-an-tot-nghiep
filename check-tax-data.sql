-- Kiểm tra dữ liệu thuế trong database

-- 1. Kiểm tra bảng tax_reports
SELECT * FROM tax_reports ORDER BY created_at DESC;

-- 2. Kiểm tra tổng thuế theo loại
SELECT 
    tax_type,
    COUNT(*) as total_reports,
    SUM(tax_amount) as total_tax,
    SUM(paid_amount) as total_paid,
    SUM(remaining_tax) as total_remaining
FROM tax_reports
GROUP BY tax_type;

-- 3. Kiểm tra báo cáo thuế theo trạng thái
SELECT 
    status,
    COUNT(*) as count,
    SUM(tax_amount) as total_tax,
    SUM(remaining_tax) as total_remaining
FROM tax_reports
GROUP BY status;

-- 4. Kiểm tra chi tiết từng báo cáo
SELECT 
    id,
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
    created_at
FROM tax_reports
ORDER BY created_at DESC
LIMIT 10;

-- 5. Kiểm tra công thức tính thuế có đúng không
SELECT 
    id,
    report_code,
    taxable_revenue,
    tax_rate,
    tax_amount,
    (taxable_revenue * tax_rate / 100) as calculated_tax,
    CASE 
        WHEN ABS(tax_amount - (taxable_revenue * tax_rate / 100)) < 0.01 THEN 'OK'
        ELSE 'SAI'
    END as validation
FROM tax_reports;
