package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

@Data
public class POItemRequest {
    private String productSku;
    private int quantity;
    private double unitPrice;
}
