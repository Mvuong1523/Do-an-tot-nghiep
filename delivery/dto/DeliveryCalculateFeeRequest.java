package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;

@Data
public class DeliveryCalculateFeeRequest {
    private String pick_province, pick_district;
    private String province, district;
    private Double weight;   // KG
    private Integer value;   // VNĐ
}
