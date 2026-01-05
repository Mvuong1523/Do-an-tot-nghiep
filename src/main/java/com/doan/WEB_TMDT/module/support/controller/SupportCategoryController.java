package com.doan.WEB_TMDT.module.support.controller;

import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.request.UpdateCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportCategoryResponse;
import com.doan.WEB_TMDT.module.support.service.SupportCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/support-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupportCategoryController {

    private final SupportCategoryService supportCategoryService;

    // Lấy danh sách category (public)
    @GetMapping
    public ResponseEntity<List<SupportCategoryResponse>> getAllCategories(
            @RequestParam(required = false, defaultValue = "true") Boolean isActive
    ) {
        return null;
    }

    // Tạo category mới
    @PostMapping
    public ResponseEntity<SupportCategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        return null;
    }

    // Cập nhật category
    @PutMapping("/{id}")
    public ResponseEntity<SupportCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return null;
    }

    // Kích hoạt/vô hiệu hoá category
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> toggleCategoryStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive
    ) {
        return null;
    }
}
