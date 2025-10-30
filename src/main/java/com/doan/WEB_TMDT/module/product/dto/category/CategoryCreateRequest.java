package com.project.ecommerce.catalog.dto.category;
// ... imports ...
@Data
public class CategoryCreateRequest {
    @NotBlank @Size(max = 50)
    private String name;
    private String description;
    private Long parentCategoryId;
    private Boolean isActive;
}