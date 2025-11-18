package com.doan.WEB_TMDT.module.product.controller;

import com.doan.WEB_TMDT.module.product.entity.Product;
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
    
    @Autowired
    private com.doan.WEB_TMDT.module.inventory.service.ProductSpecificationService productSpecificationService;

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-specs")
    public ResponseEntity<com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO> getByIdWithSpecs(@PathVariable Long id) {
        return productService.getById(id)
                .map(product -> ResponseEntity.ok(productService.toProductWithSpecs(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.create(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.update(id, product);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Search by Specifications (Cho khách hàng) =====
    
    @GetMapping("/search-by-specs")
    public ResponseEntity<List<Product>> searchBySpecs(@RequestParam String keyword) {
        // Search WarehouseProduct theo specs
        var warehouseProducts = productSpecificationService.searchBySpecValue(keyword);
        
        // Lấy Product tương ứng
        List<Product> products = warehouseProducts.stream()
                .map(wp -> wp.getProduct())
                .filter(p -> p != null)
                .toList();
        
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter-by-specs")
    public ResponseEntity<List<Product>> filterBySpecs(
            @RequestParam String key,
            @RequestParam String value
    ) {
        // Filter WarehouseProduct theo key + value
        var warehouseProducts = productSpecificationService.searchBySpecKeyAndValue(key, value);
        
        // Lấy Product tương ứng
        List<Product> products = warehouseProducts.stream()
                .map(wp -> wp.getProduct())
                .filter(p -> p != null)
                .toList();
        
        return ResponseEntity.ok(products);
    }
}
