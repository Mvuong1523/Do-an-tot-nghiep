package com.doan.WEB_TMDT.module.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProductListResponse {
    private Long id;
    private String sku;
    private String internalName;
    private String description;
    private String techSpecsJson;
    private LocalDateTime lastImportDate;
    private Long stockQuantity;
    private Long sellableQuantity;
    private String supplierName;
    private Boolean isPublished; // Đã đăng bán chưa
    private Long publishedProductId; // ID của Product nếu đã đăng bán
}
