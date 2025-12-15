-- Sửa position từ SALE thành SALES cho nhân viên bán hàng
UPDATE employees 
SET position = 'SALES' 
WHERE position = 'SALE';

-- Kiểm tra kết quả
SELECT e.id, u.email, e.position, e.full_name 
FROM employees e 
JOIN users u ON e.user_id = u.id 
WHERE e.position = 'SALES';
