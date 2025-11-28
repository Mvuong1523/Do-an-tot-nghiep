package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.dto.CategoryDTO;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAll();
    Optional<Category> getById(Long id);
    Category create(Category category);
    Category update(Long id, Category category);
    void delete(Long id);
    
    // New methods
    ApiResponse getAllCategoriesTree(); // Lấy danh mục dạng cây (có phân cấp)
    ApiResponse getActiveCategories(); // Lấy danh mục đang active
    ApiResponse getCategoryWithProducts(Long id); // Lấy danh mục kèm sản phẩm
    ApiResponse createCategory(CreateCategoryRequest request);
    ApiResponse updateCategory(Long id, CreateCategoryRequest request);
    CategoryDTO toCategoryDTO(Category category);
}
