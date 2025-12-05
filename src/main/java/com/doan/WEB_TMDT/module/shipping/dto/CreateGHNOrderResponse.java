package com.doan.WEB_TMDT.module.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGHNOrderResponse {
    private String orderCode;
    private String status;
    private LocalDateTime expectedDeliveryTime;
    private String sortCode;
    private Double totalFee;
}
