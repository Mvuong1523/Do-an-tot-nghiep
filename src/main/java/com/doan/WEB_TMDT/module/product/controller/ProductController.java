package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.module.product.entity.Product1;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product1>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product1> getById(@PathVariable Long id) {
        return productService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product1> create(@RequestBody Product1 product1) {
        return ResponseEntity.ok(productService.create(product1));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product1> update(@PathVariable Long id, @RequestBody Product1 product1) {
        Product1 updated = productService.update(id, product1);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
