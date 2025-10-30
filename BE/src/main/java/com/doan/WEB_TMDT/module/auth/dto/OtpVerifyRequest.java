package com.doan.WEB_TMDT.module.auth.dto;

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
