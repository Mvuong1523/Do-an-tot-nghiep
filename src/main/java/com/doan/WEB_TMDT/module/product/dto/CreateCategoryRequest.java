package com.doan.WEB_TMDT.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    
    private String slug;
    
    private String description;
    
    private String imageUrl;
    
    private Integer displayOrder;
    
    private Boolean active;
    
    private Long parentId; // ID của danh mục cha (null nếu là danh mục gốc)
}
