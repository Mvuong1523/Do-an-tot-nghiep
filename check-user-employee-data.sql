-- Check user data for ketoan@gmail.com
SELECT * FROM users WHERE email = 'ketoan@gmail.com';

-- Check employee data for user_id = 4
SELECT * FROM employees WHERE user_id = 4;

-- Check all employees with their positions
SELECT 
    e.id,
    e.user_id,
    u.email,
    u.full_name,
    u.role,
    e.position,
    e.first_login
FROM employees e
JOIN users u ON e.user_id = u.id
ORDER BY e.id;
