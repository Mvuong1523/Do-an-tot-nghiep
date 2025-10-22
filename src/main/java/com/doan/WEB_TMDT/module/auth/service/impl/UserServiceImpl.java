package com.doan.WEB_TMDT.module.auth.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.dto.auth.LoginRequest;
import com.doan.WEB_TMDT.common.dto.auth.LoginResponse;
import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.entity.Role;
import com.doan.WEB_TMDT.module.auth.entity.Status;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.CustomerRepository;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import com.doan.WEB_TMDT.module.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.doan.WEB_TMDT.security.JwtService;


import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    /**
     * Đăng ký tài khoản khách hàng mới
     * @param email      Email đăng ký
     * @param password   Mật khẩu đã mã hóa (hoặc chuỗi gốc sẽ tự mã hóa)
     * @param fullName   Họ tên khách hàng
     * @param phone      Số điện thoại
     * @param address    Địa chỉ
     * @return ApiResponse phản hồi
     */
    @Override
    public ApiResponse registerCustomer(String email, String password, String fullName, String phone, String address) {

        // Kiểm tra trùng email
        if (userRepository.existsByEmail(email)) {
            return ApiResponse.error("Email đã tồn tại!");
        }

        // Kiểm tra trùng số điện thoại
        if (customerRepository.existsByPhone(phone)) {
            return ApiResponse.error("Số điện thoại đã tồn tại!");
        }

        // Nếu password chưa mã hóa, thì mã hóa trước khi lưu
        String finalPassword = password.startsWith("$2a$") ? password : passwordEncoder.encode(password);

        // Tạo tài khoản người dùng
        User user = User.builder()
                .email(email)
                .password(finalPassword)
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .build();

        // Tạo hồ sơ khách hàng
        Customer customer = Customer.builder()
                .user(user)
                .fullName(fullName)
                .phone(phone)
                .address(address)
                .build();

        // Gắn mối quan hệ hai chiều
        user.setCustomer(customer);

        // Lưu vào DB
        userRepository.save(user);

        return ApiResponse.success("Tạo tài khoản khách hàng thành công!", user);
    }
    @Override
    public ApiResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) return ApiResponse.error("Email không tồn tại!");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error("Mật khẩu không đúng!");
        }

        if (user.getStatus() != Status.ACTIVE) {
            return ApiResponse.error("Tài khoản đang bị khóa!");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("uid", user.getId());
        String token = jwtService.generateToken(user.getEmail(), claims);

        return ApiResponse.success("Đăng nhập thành công!", new LoginResponse(
                token, user.getId(), user.getEmail(), user.getRole().name(), user.getStatus().name()
        ));
    }
}


