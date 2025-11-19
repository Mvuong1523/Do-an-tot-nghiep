package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.service.ProductSpecificationService;
import com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO;
import com.doan.WEB_TMDT.module.product.dto.PublishProductRequest;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductSpecificationService productSpecificationService;

    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.success("Danh sách sản phẩm", productService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return productService.getById(id)
                .map(product -> ApiResponse.success("Thông tin sản phẩm", product))
                .orElse(ApiResponse.error("Không tìm thấy sản phẩm"));
    }

    @GetMapping("/{id}/with-specs")
    public ApiResponse getByIdWithSpecs(@PathVariable Long id) {
        return productService.getById(id)
                .map(product -> {
                    ProductWithSpecsDTO dto = productService.toProductWithSpecs(product);
                    return ApiResponse.success("Thông tin sản phẩm kèm thông số", dto);
                })
                .orElse(ApiResponse.error("Không tìm thấy sản phẩm"));
    }

    // ===== Quản lý đăng bán sản phẩm từ kho (PRODUCT_MANAGER & ADMIN) =====
    
    @GetMapping("/warehouse/list")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse getWarehouseProductsForPublish() {
        return productService.getWarehouseProductsForPublish();
    }
    
    @PostMapping("/warehouse/publish")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse createProductFromWarehouse(
            @RequestBody com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest request) {
        return productService.createProductFromWarehouse(request);
    }
    
    @PutMapping("/warehouse/publish/{productId}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse updatePublishedProduct(
            @PathVariable Long productId,
            @RequestBody com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest request) {
        return productService.updatePublishedProduct(productId, request);
    }
    
    @DeleteMapping("/warehouse/unpublish/{productId}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse unpublishProduct(@PathVariable Long productId) {
        return productService.unpublishProduct(productId);
    }
    
    @PostMapping("/publish")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse publishProduct(@RequestBody PublishProductRequest request) {
        try {
            Product product = productService.publishProduct(request);
            return ApiResponse.success("Đăng bán sản phẩm thành công!", product);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ===== CRUD sản phẩm (PRODUCT_MANAGER & ADMIN) =====
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse create(@RequestBody Product product) {
        return ApiResponse.success("Tạo sản phẩm thành công", productService.create(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.update(id, product);
        return updated != null ? 
                ApiResponse.success("Cập nhật sản phẩm thành công", updated) : 
                ApiResponse.error("Không tìm thấy sản phẩm");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success("Xóa sản phẩm thành công");
    }

    // ===== Search by Specifications (Cho khách hàng) =====
    
    @GetMapping("/search-by-specs")
    public ApiResponse searchBySpecs(@RequestParam String keyword) {
        var warehouseProducts = productSpecificationService.searchBySpecValue(keyword);
        
        List<Product> products = warehouseProducts.stream()
                .map(wp -> wp.getProduct())
                .filter(p -> p != null)
                .toList();
        
        return ApiResponse.success("Tìm thấy " + products.size() + " sản phẩm", products);
    }

    @GetMapping("/filter-by-specs")
    public ApiResponse filterBySpecs(
            @RequestParam String key,
            @RequestParam String value
    ) {
        var warehouseProducts = productSpecificationService.searchBySpecKeyAndValue(key, value);
        
        List<Product> products = warehouseProducts.stream()
                .map(wp -> wp.getProduct())
                .filter(p -> p != null)
                .toList();
        
        return ApiResponse.success("Tìm thấy " + products.size() + " sản phẩm", products);
    }
}
