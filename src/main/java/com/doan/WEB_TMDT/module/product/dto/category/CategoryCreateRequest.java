package com.doan.WEB_TMDT.module.product.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ... imports ...
@Data
public class CategoryCreateRequest {
    @NotBlank
    @Size(max = 50)
    private String name;
    private String description;
    private Long parentCategoryId;
    private Boolean isActive;
}