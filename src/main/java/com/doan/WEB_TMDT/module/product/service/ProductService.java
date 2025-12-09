package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest;
import com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO;
import com.doan.WEB_TMDT.module.product.dto.PublishProductRequest;
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
    
    // Đăng bán sản phẩm từ kho
    Product publishProduct(PublishProductRequest request);
    
    // Lấy danh sách sản phẩm trong kho để đăng bán
    ApiResponse getWarehouseProductsForPublish();
    
    // Tạo Product từ WarehouseProduct
    ApiResponse createProductFromWarehouse(CreateProductFromWarehouseRequest request);
    
    // Cập nhật Product đã đăng bán
    ApiResponse updatePublishedProduct(Long productId, CreateProductFromWarehouseRequest request);
    
    // Gỡ sản phẩm khỏi trang bán (unpublish)
    ApiResponse unpublishProduct(Long productId);
    
    // === Product Images ===
    ApiResponse addProductImage(Long productId, String imageUrl, Boolean isPrimary);
    ApiResponse getProductImages(Long productId);
    ApiResponse setPrimaryImage(Long productId, Long imageId);
    ApiResponse deleteProductImage(Long imageId);
    ApiResponse reorderProductImages(Long productId, List<Long> imageIds);
    ApiResponse updateProductImage(Long imageId, com.doan.WEB_TMDT.module.product.dto.ProductImageDTO dto);
}
