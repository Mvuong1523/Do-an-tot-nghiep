package com.doan.WEB_TMDT.module.support.controller;

import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.request.SupportCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.request.UpdateCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportCategoryResponse;
import com.doan.WEB_TMDT.module.support.service.SupportCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/support-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupportCategoryController {

    private final SupportCategoryService supportCategoryService;

    @GetMapping
    public ResponseEntity<List<SupportCategoryResponse>> getAllCategories(
            @RequestParam(required = false, defaultValue = "true") Boolean isActive
    ) {
        return isActive
                ? ResponseEntity.ok(supportCategoryService.getActiveCategories())
                : ResponseEntity.ok(supportCategoryService.getAllCategories());
    }

    // Thêm mới category
    @PostMapping
    public ResponseEntity<String> createCategory(@Valid @RequestBody SupportCategoryRequest request) {
        supportCategoryService.createCategory(request);
        return ResponseEntity.status(201).body("Thêm category thành công");
    }

    // Cập nhật category
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody SupportCategoryRequest request
    ) {
        boolean updated = supportCategoryService.updateCategory(id, request);
        if (!updated) {
            return ResponseEntity.status(204).body(null);
        }
        return ResponseEntity.ok("Cập nhật category thành công");
    }

    // Set active hoặc inactive
    @PatchMapping("/{id}/active")
    public ResponseEntity<String> setActive(@PathVariable Long id) {
        boolean success = supportCategoryService.toggleActive(id);
        if (!success) {
            return ResponseEntity.status(204).body(null);
        }
        return ResponseEntity.ok(success ? "Kích hoạt thành công" : "Vô hiệu hoá thành công");
    }

    // Xóa category
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        boolean deleted = supportCategoryService.deleteCategory(id);
        if (!deleted) {
            return ResponseEntity.status(204).body(null);
        }
        return ResponseEntity.ok("Xóa category thành công");
    }
}
