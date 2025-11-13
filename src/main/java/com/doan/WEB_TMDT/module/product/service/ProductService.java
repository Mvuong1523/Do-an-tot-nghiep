package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.product.entity.Product1;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product1> getAll();
    Optional<Product1> getById(Long id);
    Product1 create(Product1 product1);
    Product1 update(Long id, Product1 product1);
    void delete(Long id);
}
