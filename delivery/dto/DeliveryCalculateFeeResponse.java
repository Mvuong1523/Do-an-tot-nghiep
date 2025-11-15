package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;

@Data
public class DeliveryCalculateFeeResponse {
    private boolean success;
    private String message;
    private Fee fee;

    @Data
    public static class Fee {
        private Integer ship_fee_only;
        private Integer insurance_fee;
        private Integer total;
    }
}
