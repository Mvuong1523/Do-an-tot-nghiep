package com.doan.WEB_TMDT.module.product.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean active;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private Integer productCount;
}
