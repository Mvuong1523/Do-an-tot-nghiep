package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.module.product.dto.*;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.service.ProductQueryService;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService;

    // --- KHÁCH HÀNG XEM DANH SÁCH & CHI TIẾT ---

    @GetMapping
    public Page<ProductDTO> search(@ModelAttribute ProductFilterRequest filter) {
        return productQueryService.searchPublic(filter);
    }

    @GetMapping("/{id}")
    public ProductDTO getDetail(@PathVariable Long id) {
        return productQueryService.getDetail(id);
    }

    // --- MÀN HÌNH QUẢN LÝ SẢN PHẨM ---

    @GetMapping("/manage")
    public Page<ProductDTO> searchManage(@ModelAttribute ProductFilterRequest filter) {
        return productQueryService.searchAdmin(filter);
    }

    @PostMapping("/from-warehouse")
    public ProductDTO createFromWarehouse(@RequestBody CreateProductFromWarehouseRequest req) {
        Product product = productService.createFromWarehouse(req);
        return ProductDTO.fromEntity(product);
    }

    @PatchMapping("/{id}/price")
    public ProductDTO updatePrice(@PathVariable Long id, @RequestBody UpdatePriceRequest req) {
        Product product = productService.updatePrice(id, req.getSalePrice());
        return ProductDTO.fromEntity(product);
    }

    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @RequestBody UpdateProductRequest req) {
        Product product = productService.updateBasicInfo(id, req);
        return ProductDTO.fromEntity(product);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.hide(id);
    }
}
