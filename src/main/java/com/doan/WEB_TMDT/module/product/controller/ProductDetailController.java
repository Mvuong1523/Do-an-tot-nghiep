package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.module.product.entity.ProductDetail;
import com.doan.WEB_TMDT.module.product.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/product-details")
public class ProductDetailController {

    @Autowired
    private ProductDetailService productDetailService;

    @GetMapping
    public ResponseEntity<List<ProductDetail>> getAll() {
        return ResponseEntity.ok(productDetailService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetail> getById(@PathVariable Long id) {
        return productDetailService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductDetail> create(@RequestBody ProductDetail productDetail) {
        return ResponseEntity.ok(productDetailService.create(productDetail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetail> update(@PathVariable Long id, @RequestBody ProductDetail productDetail) {
        ProductDetail updated = productDetailService.update(id, productDetail);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
