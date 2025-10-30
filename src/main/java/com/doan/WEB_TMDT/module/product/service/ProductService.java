package com.project.ecommerce.catalog.service;
// ... imports ...
public interface ProductService {
    Product createProduct(ProductCreateRequest request); // CREATE
    // Có thể thêm Product getProductById(Long id); // READ
    Product updateProduct(Long id, ProductCreateRequest request); // UPDATE
    void deleteProduct(Long id); // DELETE (Soft-delete)
}