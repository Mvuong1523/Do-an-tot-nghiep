package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;
import java.util.List;

@Data
public class DeliveryCreateOrderResponse {
    private boolean success;
    private String message;
    private OrderInfo order;
    private ErrorInfo error;

    @Data
    public static class OrderInfo {
        private String partner_id, label, area, fee, insurance_fee;
        private Long tracking_id;
        private String estimated_pick_time, estimated_deliver_time;
        private List<Object> products;
        private Integer status_id;
    }

    @Data
    public static class ErrorInfo {
        private String code, partner_id, ghtk_label, created;
        private Integer status;
    }
}
