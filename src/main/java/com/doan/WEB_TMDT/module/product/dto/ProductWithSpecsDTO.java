package com.doan.WEB_TMDT.module.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithSpecsDTO {
    private Long id;
    private String name;
    private String sku;
    private Double price;
    private String description;
    private String imageUrl;
    private Long stockQuantity;
    private String categoryName;
    
    // Thông số kỹ thuật dạng Map để dễ hiển thị
    private Map<String, String> specifications;
}
