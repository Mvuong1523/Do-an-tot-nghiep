package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO;
import com.doan.WEB_TMDT.module.product.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAll();
    Optional<Product> getById(Long id);
    Product create(Product product);
    Product update(Long id, Product product);
    void delete(Long id);
    
    // Convert Product → DTO kèm specifications
    ProductWithSpecsDTO toProductWithSpecs(Product product);
}
