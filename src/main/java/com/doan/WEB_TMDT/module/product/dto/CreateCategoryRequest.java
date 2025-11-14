package com.doan.WEB_TMDT.module.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {
    private String name;
    private String description;
}
