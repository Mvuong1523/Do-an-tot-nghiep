package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import com.doan.WEB_TMDT.module.product.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-details")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.success("Danh sách chi tiết sản phẩm", productDetailService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return productDetailService.getById(id)
                .map(detail -> ApiResponse.success("Thông tin chi tiết", detail))
                .orElse(ApiResponse.error("Không tìm thấy"));
    }

    @PostMapping
    public ApiResponse create(@RequestBody ProductDetail productDetail) {
        return ApiResponse.success("Tạo thành công", productDetailService.create(productDetail));
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody ProductDetail productDetail) {
        ProductDetail updated = productDetailService.update(id, productDetail);
        return updated != null ? 
                ApiResponse.success("Cập nhật thành công", updated) : 
                ApiResponse.error("Không tìm thấy");
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        productDetailService.delete(id);
        return ApiResponse.success("Xóa thành công");
    }
}
