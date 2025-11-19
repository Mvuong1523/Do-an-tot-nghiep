// com.doan.WEB_TMDT.module.auth.dto.LoginResponse
package com.doan.WEB_TMDT.module.auth.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Long   userId;
    private String email;
    private String fullName;
    private String role;
    private String position; // WAREHOUSE, PRODUCT_MANAGER, etc.
    private String status;
}
