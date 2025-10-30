package com.project.ecommerce.catalog.dto.product;
// ... imports ...
@Data
public class VariantRequest {
    private Long id;
    @NotBlank private String sku;
    private String attributes;
    @Min(0) private Integer initialStock;
    @Min(0) private BigDecimal sellingPrice;
}