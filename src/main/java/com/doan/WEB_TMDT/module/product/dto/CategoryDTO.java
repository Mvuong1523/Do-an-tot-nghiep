package com.doan.WEB_TMDT.module.product.dto;

import com.doan.WEB_TMDT.module.product.entity.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private String description;

    public static CategoryDTO fromEntity(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build();
    }
}
