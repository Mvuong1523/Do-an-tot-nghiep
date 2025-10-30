package com.project.ecommerce.catalog.service;

import com.project.ecommerce.catalog.dto.product.ProductCreateRequest;
import com.project.ecommerce.catalog.entity.Product;
import com.project.ecommerce.catalog.entity.Variant; // Cần import Variant

public interface ProductService {
    // CRUD đã có...
    Product createProduct(ProductCreateRequest request);
    Product updateProduct(Long id, ProductCreateRequest request);
    void deleteProduct(Long id);

    // PHƯƠNG THỨC BỔ SUNG: Đọc Variant theo SKU
    Variant getVariantBySku(String sku); // READ
}