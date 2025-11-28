package com.doan.WEB_TMDT.module.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String paymentCode;
    private Double amount;
    private String status;
    
    // SePay info
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String content;
    private String qrCodeUrl;
    
    private String expiredAt;
    private String message;
}
