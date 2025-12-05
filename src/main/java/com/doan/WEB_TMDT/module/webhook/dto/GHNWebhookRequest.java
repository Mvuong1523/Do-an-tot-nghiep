package com.doan.WEB_TMDT.module.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GHNWebhookRequest {
    private String orderCode;
    private String status;
    private String statusText;
    private Long updatedDate;
    private String currentWarehouse;
    private String description;
    private String reason;
    private Double codAmount;
    private Double shippingFee;
    private String partnerCode; // Mã đơn hàng của shop
}
