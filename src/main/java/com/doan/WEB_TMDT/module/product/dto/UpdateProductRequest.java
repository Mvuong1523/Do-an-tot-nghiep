package com.doan.WEB_TMDT.module.product.dto;

import com.doan.WEB_TMDT.module.product.entity.ProductShowStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

    private String name;
    private String brand;
    private String model;

    private Long salePrice;
    private Long originalPrice;

    private Long categoryId;

    private String description;
    private String imageUrl;

    private ProductShowStatus status;
}
