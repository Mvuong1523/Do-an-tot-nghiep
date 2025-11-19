package com.doan.WEB_TMDT.module.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận webhook từ SePay khi có giao dịch thành công
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepayWebhookRequest {
    private String transactionId;
    private String bankCode;
    private String accountNumber;
    private Double amount;
    private String content;
    private String transactionDate;
    private String status;
    private String signature; // Chữ ký xác thực
}
