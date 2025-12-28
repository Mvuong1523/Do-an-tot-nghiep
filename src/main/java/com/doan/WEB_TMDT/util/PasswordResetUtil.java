package com.doan.WEB_TMDT.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility để generate BCrypt password hash
 * Chạy class này để tạo hash cho mật khẩu mới
 */
public class PasswordResetUtil {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Danh sách mật khẩu cần mã hóa
        String[] passwords = {
            "123456",
            "password123", 
            "admin123",
            "12345678"
        };
        
        System.out.println("=== BCrypt Password Hashes ===\n");
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Mật khẩu: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
        
        System.out.println("=== Hướng dẫn sử dụng ===");
        System.out.println("1. Copy hash của mật khẩu bạn muốn");
        System.out.println("2. Chạy SQL: UPDATE customers SET password = '<hash>' WHERE email = 'your-email@example.com';");
        System.out.println("3. Đăng nhập với mật khẩu mới");
        System.out.println("4. Đổi mật khẩu ngay sau khi đăng nhập!");
    }
}
