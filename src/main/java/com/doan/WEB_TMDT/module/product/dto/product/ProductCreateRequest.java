package com.project.ecommerce.catalog.dto.product;
// ... imports ...
@Data
public class ProductCreateRequest {
    @NotBlank private String name;
    @NotBlank private String slug;
    @NotNull private Long categoryId;
    private String description;
    @Min(0) private BigDecimal listPrice;
    @Valid @NotEmpty
    private List<VariantRequest> variants;
}