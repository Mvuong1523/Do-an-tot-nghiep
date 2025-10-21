package com.doan.WEB_TMDT.common.dto.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String dateOfBirth;
    private String address;
}
