package com.doan.WEB_TMDT.module.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SepayIPNRequest {
    private String merchant_id;
    private String order_code;
    private BigDecimal amount;
    private String status;
    private String txn_id;
    private String signature;
}
