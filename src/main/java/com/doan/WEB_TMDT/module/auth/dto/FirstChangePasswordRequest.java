package com.doan.WEB_TMDT.module.auth.dto;

import lombok.Data;

@Data
public class FirstChangePasswordRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;
}
