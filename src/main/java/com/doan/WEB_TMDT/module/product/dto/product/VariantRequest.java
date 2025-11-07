package com.doan.WEB_TMDT.module.product.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

// ... imports ...
@Data
public class VariantRequest {
    private Long id;
    @NotBlank
    private String sku;
    private String attributes;
    @Min(0) private Integer initialStock;
    @Min(0) private BigDecimal sellingPrice;
}