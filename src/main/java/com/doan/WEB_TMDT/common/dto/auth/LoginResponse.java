// com.doan.WEB_TMDT.common.dto.auth.LoginResponse
package com.doan.WEB_TMDT.common.dto.auth;
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
