-- Kiểm tra bảng employee_registration có tồn tại không
SHOW TABLES LIKE 'employee_registration';

-- Kiểm tra cấu trúc bảng
DESCRIBE employee_registration;

-- Kiểm tra dữ liệu trong bảng
SELECT * FROM employee_registration;

-- Đếm số lượng records
SELECT COUNT(*) as total_registrations FROM employee_registration;

-- Kiểm tra các email đã đăng ký
SELECT email, phone, full_name, position, approved, created_at 
FROM employee_registration 
ORDER BY created_at DESC;
