package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;

public interface CategoryService {
    ApiResponse createCategory(CreateCategoryRequest req);
    ApiResponse getAllCategories();
}
