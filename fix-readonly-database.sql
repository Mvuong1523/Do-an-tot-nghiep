-- Kiểm tra xem database có đang ở chế độ read-only không
SHOW VARIABLES LIKE 'read_only';
SHOW VARIABLES LIKE 'super_read_only';

-- Tắt read-only mode (chạy với quyền root)
SET GLOBAL read_only = 0;
SET GLOBAL super_read_only = 0;

-- Kiểm tra lại
SHOW VARIABLES LIKE 'read_only';
SHOW VARIABLES LIKE 'super_read_only';

-- Kiểm tra quyền của user root
SHOW GRANTS FOR 'root'@'localhost';

-- Nếu cần, cấp lại quyền đầy đủ cho user root
GRANT ALL PRIVILEGES ON web3.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
