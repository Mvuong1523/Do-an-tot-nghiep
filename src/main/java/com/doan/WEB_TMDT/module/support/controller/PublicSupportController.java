package com.doan.WEB_TMDT.module.support.controller;


import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.support.dto.response.SupportCategoryResponse;
import com.doan.WEB_TMDT.module.support.service.SupportCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller public cho các endpoint không cần authentication
 */
@RestController
@RequestMapping("/api/public/support")
@RequiredArgsConstructor
public class PublicSupportController {

    private final SupportCategoryService categoryService;

    /**
     * Lấy danh sách loại yêu cầu hỗ trợ (để hiển thị trong form)
     * GET /api/public/support/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getActiveCategories() {
        List<SupportCategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách loại yêu cầu thành công", categories));
    }
}