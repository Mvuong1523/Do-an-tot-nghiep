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
public class GHNOrderDetailResponse {
    private String orderCode;
    private String status;
    private String statusText;
    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime updatedDate;
    private String currentWarehouse;
    private String currentStatus;
    private Double codAmount;
    private Double shippingFee;
    private String note;
    
    // Log history
    private java.util.List<StatusLog> logs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusLog {
        private String status;
        private String statusText;
        private LocalDateTime time;
        private String location;
    }
}
