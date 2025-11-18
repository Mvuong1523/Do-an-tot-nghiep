package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.success("Danh sách danh mục", categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return categoryService.getById(id)
                .map(category -> ApiResponse.success("Thông tin danh mục", category))
                .orElse(ApiResponse.error("Không tìm thấy danh mục"));
    }

    @PostMapping
    public ApiResponse create(@RequestBody Category category) {
        return ApiResponse.success("Tạo danh mục thành công", categoryService.create(category));
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.update(id, category);
        return updated != null ? 
                ApiResponse.success("Cập nhật danh mục thành công", updated) : 
                ApiResponse.error("Không tìm thấy danh mục");
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success("Xóa danh mục thành công");
    }
}
