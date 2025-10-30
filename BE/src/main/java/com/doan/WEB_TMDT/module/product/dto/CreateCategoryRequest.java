package com.doan.WEB_TMDT.module.product.dto;

import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String name;
    private String description;
    private Long parentId; // nếu có danh mục cha
}
