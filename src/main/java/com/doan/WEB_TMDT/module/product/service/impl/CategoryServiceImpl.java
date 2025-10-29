package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.repository.CategoryRepository;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;

    @Override
    public ApiResponse createCategory(CreateCategoryRequest req) {
        if (categoryRepo.existsByName(req.getName())) {
            return ApiResponse.error("Danh mục đã tồn tại!");
        }

        Category parent = null;
        if (req.getParentId() != null) {
            parent = categoryRepo.findById(req.getParentId()).orElse(null);
        }

        Category category = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .parent(parent)
                .build();

        categoryRepo.save(category);
        return ApiResponse.success("Tạo danh mục thành công!", category);
    }

    @Override
    public ApiResponse getAllCategories() {
        return ApiResponse.success("Danh sách danh mục", categoryRepo.findAll());
    }
}
