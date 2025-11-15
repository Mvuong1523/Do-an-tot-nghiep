package com.doan.WEB_TMDT.module.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private BigDecimal amount;
    private String returnUrl;
    private String notifyUrl;
}





