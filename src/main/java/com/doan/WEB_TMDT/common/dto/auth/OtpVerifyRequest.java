package com.doan.WEB_TMDT.common.dto.auth;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String otpCode;
}
