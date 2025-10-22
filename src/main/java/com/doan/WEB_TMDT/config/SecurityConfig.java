package com.doan.WEB_TMDT.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Cho phép truy cập API công khai như đăng ký, OTP, đăng nhập
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF để test API dễ hơn
                .authorizeHttpRequests(auth -> auth
                        // Cho phép các endpoint công khai không cần đăng nhập
                        .requestMatchers("/api/auth/**").permitAll()

                        // Các API khác yêu cầu đăng nhập
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // Dùng để mã hoá mật khẩu người dùng khi lưu DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
