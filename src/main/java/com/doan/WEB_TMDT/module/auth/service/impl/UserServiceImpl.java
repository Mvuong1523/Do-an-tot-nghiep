package com.doan.WEB_TMDT.module.auth.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.dto.FirstChangePasswordRequest;
import com.doan.WEB_TMDT.module.auth.dto.ChangePasswordRequest;
import com.doan.WEB_TMDT.module.auth.dto.LoginRequest;
import com.doan.WEB_TMDT.module.auth.dto.LoginResponse;
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
        System.out.println("=== LOGIN START ===");
        System.out.println("Email: " + request.getEmail());
        
        var user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            System.out.println("❌ Email không tồn tại!");
            return ApiResponse.error("Email không tồn tại!");
        }
        
        System.out.println("✅ User found: " + user.getEmail() + ", Role: " + user.getRole());
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("❌ Mật khẩu không đúng!");
            return ApiResponse.error("Mật khẩu không đúng!");
        }
        
        System.out.println("✅ Password matched");
        
        if (user.getStatus() != Status.ACTIVE) {
            System.out.println("❌ Tài khoản bị khóa!");
            return ApiResponse.error("Tài khoản đang bị khóa!");
        }
        
        System.out.println("✅ Account active");
        
        if (user.getRole() == Role.EMPLOYEE && user.getEmployee() != null) {
            if (user.getEmployee().isFirstLogin()) {
                System.out.println("⚠️ First login - require password change");
                return ApiResponse.success("Đăng nhập lần đầu. Yêu cầu đổi mật khẩu!",
                        Map.of("requireChangePassword", true, "email", user.getEmail()));
            }
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        if (user.getEmployee() != null && user.getEmployee().getPosition() != null) {
            claims.put("position", user.getEmployee().getPosition().name());
        }

        System.out.println("🔑 Generating JWT token...");
        String token = jwtService.generateToken(user.getEmail(), claims);
        System.out.println("✅ Token generated: " + token.substring(0, 20) + "...");

        // Get fullName and position
        String fullName = null;
        String position = null;
        
        if (user.getCustomer() != null) {
            fullName = user.getCustomer().getFullName();
        } else if (user.getEmployee() != null) {
            fullName = user.getEmployee().getFullName();
            position = user.getEmployee().getPosition() != null ? 
                      user.getEmployee().getPosition().name() : null;
        }
        
        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                fullName,
                user.getRole().name(),
                position,
                user.getStatus().name()
        );
        
        System.out.println("✅ Login successful! Position: " + position);
        System.out.println("=== LOGIN END ===");
        
        return ApiResponse.success("Đăng nhập thành công!", response);
    }

    @Override
    public ApiResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.error("Mật khẩu cũ không đúng!");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.error("Xác nhận mật khẩu mới không khớp!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success("Đổi mật khẩu thành công!");
    }

    @Override
    public ApiResponse firstChangePassword(FirstChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // Chỉ áp dụng cho nhân viên
        if (user.getRole() != Role.EMPLOYEE || user.getEmployee() == null) {
            return ApiResponse.error("Chỉ nhân viên mới được đổi mật khẩu lần đầu!");
        }

        Employee emp = user.getEmployee();

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ApiResponse.error("Mật khẩu hiện tại không đúng!");
        }

        // Kiểm tra mật khẩu mới khớp
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.error("Xác nhận mật khẩu mới không khớp!");
        }

        // Đổi mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        emp.setFirstLogin(false); // ✅ Đánh dấu đã đổi mật khẩu
        userRepository.save(user);

        return ApiResponse.success("Đổi mật khẩu thành công!");
    }

    @Override
    public ApiResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        String fullName = null;
        String position = null;
        
        if (user.getCustomer() != null) {
            fullName = user.getCustomer().getFullName();
        } else if (user.getEmployee() != null) {
            fullName = user.getEmployee().getFullName();
            position = user.getEmployee().getPosition() != null ? 
                      user.getEmployee().getPosition().name() : null;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullName", fullName);
        userData.put("role", user.getRole().name());
        userData.put("position", position);
        userData.put("status", user.getStatus().name());

        return ApiResponse.success("Lấy thông tin người dùng thành công", userData);
    }
}
