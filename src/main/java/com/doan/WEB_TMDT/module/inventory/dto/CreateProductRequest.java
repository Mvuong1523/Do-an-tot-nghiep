package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

@Data
public class CreateProductRequest {
    private String sku;
    private String name;
    private String brand;
    private String category;
    private String unit;
    private Long price;
}
