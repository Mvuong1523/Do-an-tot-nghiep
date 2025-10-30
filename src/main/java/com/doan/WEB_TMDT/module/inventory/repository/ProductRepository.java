package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
}