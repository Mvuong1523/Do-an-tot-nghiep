package com.doan.WEB_TMDT.module.auth.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.dto.auth.LoginRequest;
import com.doan.WEB_TMDT.common.dto.auth.LoginResponse;
import com.doan.WEB_TMDT.module.auth.entity.*;
import com.doan.WEB_TMDT.module.auth.repository.*;
import com.doan.WEB_TMDT.module.auth.service.UserService;
import com.doan.WEB_TMDT.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ApiResponse registerCustomer(String email, String password, String fullName, String phone, String address) {

        if (userRepository.existsByEmail(email)) {
            return ApiResponse.error("Email đã tồn tại!");
        }
        if (customerRepository.existsByPhone(phone)) {
            return ApiResponse.error("Số điện thoại đã tồn tại!");
        }

        String finalPassword = password.startsWith("$2a$") ? password : passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(finalPassword)
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .build();

        Customer customer = Customer.builder()
                .user(user)
                .fullName(fullName)
                .phone(phone)
                .address(address)
                .build();

        user.setCustomer(customer);
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
        if (user.getEmployee() != null && user.getEmployee().getPosition() != null) {
            claims.put("position", user.getEmployee().getPosition().name());
        }

        String token = jwtService.generateToken(user.getEmail(), claims);

        return ApiResponse.success("Đăng nhập thành công!", new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        ));
    }
}
