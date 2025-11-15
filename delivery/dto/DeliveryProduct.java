package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;

@Data
public class DeliveryProduct {
    private String name;
    private Double weight;      // KG
    private Integer quantity;
    private String product_code;
    private Integer price;
    private Double height;
    private Double width;
    private Double length;
}
