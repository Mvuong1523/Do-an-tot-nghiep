package com.doan.WEB_TMDT.module.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String paymentCode;
    private String qrBase64;
}
