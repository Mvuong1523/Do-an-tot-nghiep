package com.doan.WEB_TMDT.module.auth.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.dto.auth.OtpVerifyRequest;
import com.doan.WEB_TMDT.common.dto.auth.RegisterRequest;
import com.doan.WEB_TMDT.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/send-otp")
    public ApiResponse sendOtp(@RequestBody RegisterRequest request) {
        return authService.sendOtp(request);
    }

    @PostMapping("/register/verify")
    public ApiResponse verifyOtp(@RequestBody OtpVerifyRequest request) {
        return authService.verifyOtpAndRegister(request);
    }
}
