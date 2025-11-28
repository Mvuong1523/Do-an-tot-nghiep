package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Public endpoints (cho khách hàng)
    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.success("Danh sách danh mục", categoryService.getAll());
    }

    @GetMapping("/tree")
    public ApiResponse getCategoriesTree() {
        return categoryService.getAllCategoriesTree();
    }

    @GetMapping("/active")
    public ApiResponse getActiveCategories() {
        return categoryService.getActiveCategories();
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return categoryService.getCategoryWithProducts(id);
    }

    // Product Manager & Admin endpoints
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse create(@Valid @RequestBody CreateCategoryRequest request, 
                              org.springframework.security.core.Authentication authentication) {
        System.out.println("=== CREATE CATEGORY ===");
        System.out.println("User: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Authorities: " + (authentication != null ? authentication.getAuthorities() : "null"));
        return categoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success("Xóa danh mục thành công");
    }
    
    // Test endpoint để kiểm tra authorities
    @GetMapping("/test-auth")
    public ApiResponse testAuth(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ApiResponse.error("Not authenticated");
        }
        
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("name", authentication.getName());
        info.put("authorities", authentication.getAuthorities().toString());
        info.put("authenticated", authentication.isAuthenticated());
        
        System.out.println("=== TEST AUTH ===");
        System.out.println("User: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        
        return ApiResponse.success("Auth info", info);
    }
}
