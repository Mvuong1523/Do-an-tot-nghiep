package com.doan.WEB_TMDT.common.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otpCode;
}
