package com.doan.WEB_TMDT.module.auth.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.*;
import com.doan.WEB_TMDT.module.auth.repository.*;
import com.doan.WEB_TMDT.module.auth.service.EmployeeRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmployeeRegistrationServiceImpl implements EmployeeRegistrationService {

    private final EmployeeRegistrationRepository registrationRepo;
    private final EmployeeRepository employeeRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public ApiResponse registerEmployee(String fullName, String email, String phone, String address, Position position, String note) {
        if (registrationRepo.existsByEmail(email) || userRepo.existsByEmail(email)) {
            return ApiResponse.error("Email đã tồn tại hoặc đang chờ duyệt!");
        }

        if (registrationRepo.existsByPhone(phone)) {
            return ApiResponse.error("Số điện thoại đã tồn tại hoặc đang chờ duyệt!");
        }

        EmployeeRegistration reg = EmployeeRegistration.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .address(address)
                .position(position)
                .note(note)
                .approved(false)
                .createdAt(LocalDateTime.now())
                .build();
        registrationRepo.save(reg);

        return ApiResponse.success("Gửi yêu cầu đăng ký nhân viên thành công, chờ admin duyệt!", reg);
    }

    @Transactional
    @Override
    public ApiResponse approveEmployee(Long registrationId) {
        EmployeeRegistration reg = registrationRepo.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đăng ký!"));

        if (reg.isApproved()) return ApiResponse.error("Phiếu đăng ký này đã được duyệt!");

        // Tạo mật khẩu ngẫu nhiên
        String rawPassword = generateRandomPassword(10);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Tạo tài khoản user
        User user = User.builder()
                .email(reg.getEmail())
                .password(encodedPassword)
                .role(Role.EMPLOYEE)
                .status(Status.ACTIVE)
                .build();

        // Tạo hồ sơ nhân viên chính thức
        Employee emp = Employee.builder()
                .user(user)
                .fullName(reg.getFullName())
                .phone(reg.getPhone())
                .address(reg.getAddress())
                .position(reg.getPosition())
                .build();
        user.setEmployee(emp);

        userRepo.save(user); // cascade lưu cả employee

        // Cập nhật trạng thái phiếu
        reg.setApproved(true);
        reg.setApprovedAt(LocalDateTime.now());
        registrationRepo.save(reg);

        // Gửi mail thông báo tài khoản
        sendEmailAccount(reg.getEmail(), rawPassword);

        return ApiResponse.success("Đã duyệt và gửi thông tin tài khoản qua email!", emp);
    }

    private String generateRandomPassword(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendEmailAccount(String email, String password) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Tài khoản nhân viên đã được duyệt");
            msg.setText("Xin chào,\n\nTài khoản của bạn đã được duyệt.\n" +
                    "Email: " + email + "\n" +
                    "Mật khẩu: " + password + "\n\n" +
                    "Vui lòng đăng nhập và đổi mật khẩu sau khi đăng nhập.\n\nTrân trọng,\nAdmin");
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Không thể gửi email cho " + email + ": " + e.getMessage());
        }
    }
}
