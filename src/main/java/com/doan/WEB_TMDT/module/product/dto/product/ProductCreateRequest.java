package com.doan.WEB_TMDT.module.product.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// ... imports ...
@Data
public class ProductCreateRequest {
    @NotBlank
    private String name;
    @NotBlank private String slug;
    @NotNull
    private Long categoryId;
    private String description;
    @Min(0) private BigDecimal listPrice;
    @Valid
    @NotEmpty
    private List<com.project.ecommerce.catalog.dto.product.VariantRequest> variants;
}