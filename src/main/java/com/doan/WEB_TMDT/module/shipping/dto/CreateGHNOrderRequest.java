package com.doan.WEB_TMDT.module.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGHNOrderRequest {
    private String toName;
    private String toPhone;
    private String toAddress;
    private String toWardCode;
    private Integer toDistrictId;
    private String note;
    private Integer codAmount;
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
    private Integer serviceTypeId;
    private Integer paymentTypeId;
    private List<GHNOrderItem> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GHNOrderItem {
        private String name;
        private String code;
        private Integer quantity;
        private Integer price;
    }
}
