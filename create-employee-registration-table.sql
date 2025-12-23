-- Tạo bảng employee_registration nếu chưa có
CREATE TABLE IF NOT EXISTS employee_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(500),
    position VARCHAR(50) NOT NULL,
    note TEXT,
    approved BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    approved_at DATETIME,
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_approved (approved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Kiểm tra bảng đã tạo thành công
DESCRIBE employee_registration;

-- Kiểm tra dữ liệu
SELECT * FROM employee_registration;
