-- Kiểm tra nhanh dữ liệu thuế

-- 1. Có bao nhiêu báo cáo thuế?
SELECT COUNT(*) as total_reports FROM tax_reports;

-- 2. Xem 5 báo cáo gần nhất
SELECT 
    id,
    report_code,
    tax_type,
    taxable_revenue,
    tax_rate,
    tax_amount,
    remaining_tax,
    status
FROM tax_reports
ORDER BY created_at DESC
LIMIT 5;

-- 3. Kiểm tra bảng có tồn tại không?
SHOW TABLES LIKE 'tax_reports';

-- 4. Kiểm tra cấu trúc bảng
DESCRIBE tax_reports;
