-- Fix warehouse user role from WAREHOUSE to EMPLOYEE
-- The correct structure is: role = EMPLOYEE, position = WAREHOUSE

UPDATE users 
SET role = 'EMPLOYEE' 
WHERE role = 'WAREHOUSE';

-- Verify the change
SELECT u.id, u.email, u.role, e.position 
FROM users u 
LEFT JOIN employees e ON u.id = e.user_id 
WHERE e.position = 'WAREHOUSE';
