package com.doan.WEB_TMDT.module.support.dto.response;

import com.doan.WEB_TMDT.module.support.entities.SupportCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportCategoryResponse {

    private Long id;

    private String name;

    private String description;

    public SupportCategoryResponse(SupportCategory supportCategory) {
        this.id = supportCategory.getId();
        this.name = supportCategory.getName();
        this.description = supportCategory.getDescription();
    }
}

