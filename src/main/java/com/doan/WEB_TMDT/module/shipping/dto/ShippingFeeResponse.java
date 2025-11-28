package com.doan.WEB_TMDT.module.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeResponse {
    private Double fee;
    private String shipMethod; // INTERNAL (shipper riêng) hoặc GHTK
    private String estimatedTime; // Thời gian dự kiến
    private Boolean isFreeShip;
}
