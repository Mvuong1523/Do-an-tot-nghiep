package com.doan.WEB_TMDT.common.dto.auth;
import lombok.Data;
@Data
public class LoginRequest {
    private String email;
    private String password;
}
