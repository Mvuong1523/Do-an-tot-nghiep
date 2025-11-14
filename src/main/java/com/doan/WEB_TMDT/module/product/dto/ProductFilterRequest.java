package com.doan.WEB_TMDT.module.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterRequest {

    private Long categoryId;
    private String keyword;   // tên, brand, model
    private Long minPrice;
    private Long maxPrice;
    private Boolean onlyInStock;
    private String sortBy;    // priceAsc, priceDesc, newest

    private int page = 0;
    private int size = 20;
}
