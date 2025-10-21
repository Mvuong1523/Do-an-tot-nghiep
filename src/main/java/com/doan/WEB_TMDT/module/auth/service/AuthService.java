package com.doan.WEB_TMDT.module.auth.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.dto.auth.OtpVerifyRequest;
import com.doan.WEB_TMDT.common.dto.auth.RegisterRequest;

public interface AuthService {
    ApiResponse sendOtp(RegisterRequest request);
    ApiResponse verifyOtpAndRegister(OtpVerifyRequest request);
}
