// com.doan.WEB_TMDT.module.auth.dto.LoginResponse
package com.doan.WEB_TMDT.module.auth.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long   userId;
    private String email;
    private String role;
    private String status;
}
