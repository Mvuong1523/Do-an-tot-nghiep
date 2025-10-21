package com.doan.WEB_TMDT.module.auth.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.dto.auth.OtpVerifyRequest;
import com.doan.WEB_TMDT.common.dto.auth.RegisterRequest;
import com.doan.WEB_TMDT.module.auth.entity.*;
import com.doan.WEB_TMDT.module.auth.repository.*;
import com.doan.WEB_TMDT.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public ApiResponse sendOtp(RegisterRequest request) {
        // Kiểm tra email hoặc SĐT tồn tại
        if (authRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email đã được sử dụng!");
        }
        if (authRepository.existsByPhone(request.getPhone())) {
            return ApiResponse.error("Số điện thoại đã tồn tại!");
        }

        // Tạo mã OTP ngẫu nhiên 6 chữ số
        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpVerification otpVerification = OtpVerification.builder()
                .email(request.getEmail())
                .otpCode(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .verified(false)
                .build();
        otpRepository.save(otpVerification);

        // Gửi email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getEmail());
        message.setSubject("Mã xác minh đăng ký tài khoản");
        message.setText("Xin chào " + request.getFullName() +
                ",\n\nMã OTP của bạn là: " + otp +
                "\nMã có hiệu lực trong 2 phút.\n\nTrân trọng.");
        mailSender.send(message);

        return ApiResponse.success("Mã OTP đã được gửi đến email của bạn!");
    }

    @Override
    public ApiResponse verifyOtpAndRegister(OtpVerifyRequest request) {
        var otpRecord = otpRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElse(null);

        if (otpRecord == null) {
            return ApiResponse.error("Mã OTP không hợp lệ!");
        }

        if (otpRecord.isVerified()) {
            return ApiResponse.error("Mã OTP này đã được xác minh!");
        }

        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Mã OTP đã hết hạn!");
        }

        otpRecord.setVerified(true);
        otpRepository.save(otpRecord);

        // Lấy lại thông tin đăng ký ban đầu từ form (ở thực tế bạn nên lưu tạm hoặc dùng Redis)
        User user = User.builder()
                .username(request.getEmail()) // hoặc tự sinh
                .email(request.getEmail())
                .password(passwordEncoder.encode("123456")) // hoặc lấy từ RegisterRequest
                .role(Role.CUSTOMER)
                .build();

        authRepository.save(user);

        return ApiResponse.success("Xác minh thành công! Tài khoản đã được tạo.");
    }
}
