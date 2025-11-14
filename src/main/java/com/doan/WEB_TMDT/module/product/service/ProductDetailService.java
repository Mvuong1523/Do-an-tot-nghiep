package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;

import java.util.List;
import java.util.Optional;

public interface ProductDetailService {
    List<ProductDetail> getAll();
    Optional<ProductDetail> getById(Long id);
    ProductDetail create(ProductDetail productDetail);
    ProductDetail update(Long id, ProductDetail productDetail);
    void delete(Long id);
}
