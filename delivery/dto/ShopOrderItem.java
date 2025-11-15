package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;

@Data
public class ShopOrderItem {
    private String productName;
    private Integer quantity;
    private Integer unitPrice;   // optional
    private Double weight;       // KG (null -> default 0.1)
}
