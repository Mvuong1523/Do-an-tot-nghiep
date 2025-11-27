package com.doan.WEB_TMDT.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private String transactionId;
    
    @JsonProperty("gateway")
    private String bankCode;
    
    private String accountNumber;
    
    @JsonProperty("transferAmount")
    private Double amount;
    
    private String content;
    
    @JsonProperty("transactionDate")
    private String transactionDate;
    
    private String status;
    
    private String signature; // Chữ ký xác thực
    
    private String transferType;
    private String description;
    private String referenceCode;
    private String subAccount;
    private String code;
}
