package com.doan.WEB_TMDT.module.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductFromWarehouseRequest {

    private Long warehouseProductId;

    private String name;
    private String brand;
    private String model;

    private Long salePrice;
    private Long originalPrice;

    private Long categoryId;

    private String description;
    private String imageUrl;
}
