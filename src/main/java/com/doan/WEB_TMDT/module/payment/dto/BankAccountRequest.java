package com.doan.WEB_TMDT.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequest {
    
    @NotBlank(message = "Mã ngân hàng không được để trống")
    private String bankCode;
    
    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;
    
    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;
    
    @NotBlank(message = "Tên tài khoản không được để trống")
    private String accountName;
    
    private String description;
    
    private String sepayApiToken; // API token từ SePay (optional)
    
    private String sepayMerchantId; // Merchant ID từ SePay (optional)
    
    private Boolean isActive;
    
    private Boolean isDefault;
}
