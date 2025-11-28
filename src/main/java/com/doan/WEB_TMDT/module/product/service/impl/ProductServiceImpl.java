package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.inventory.entity.ProductSpecification;
import com.doan.WEB_TMDT.module.inventory.repository.WarehouseProductRepository;
import com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO;
import com.doan.WEB_TMDT.module.product.dto.PublishProductRequest;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.repository.CategoryRepository;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final CategoryRepository categoryRepository;
    private final com.doan.WEB_TMDT.module.inventory.repository.InventoryStockRepository inventoryStockRepository;

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        if (productRepository.existsById(id)) {
            product.setId(id);
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductWithSpecsDTO toProductWithSpecs(Product product) {
        var dto = ProductWithSpecsDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();

        // Lấy specifications từ techSpecsJson của Product
        if (product.getTechSpecsJson() != null && !product.getTechSpecsJson().isEmpty()) {
            try {
                // Parse JSON string thành Map
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, String> specs = mapper.readValue(
                    product.getTechSpecsJson(), 
                    Map.class
                );
                dto.setSpecifications(specs);
            } catch (Exception e) {
                System.err.println("Error parsing techSpecsJson: " + e.getMessage());
            }
        }

        return dto;
    }

    @Override
    public Product publishProduct(PublishProductRequest request) {
        // 1. Lấy WarehouseProduct
        var warehouseProduct = warehouseProductRepository.findById(request.getWarehouseProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong kho với ID: " + request.getWarehouseProductId()));

        // 2. Kiểm tra đã đăng bán chưa
        if (warehouseProduct.getProduct() != null) {
            throw new RuntimeException("Sản phẩm này đã được đăng bán rồi!");
        }

        // 3. Lấy Category
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // 4. Tạo Product mới
        Product product = Product.builder()
                .name(request.getName())
                .sku(warehouseProduct.getSku())
                .price(request.getPrice())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .category(category)
                .warehouseProduct(warehouseProduct)
                .stockQuantity(warehouseProduct.getQuantityInStock())
                .build();

        // 5. Lưu Product
        return productRepository.save(product);
    }

    @Override
    public com.doan.WEB_TMDT.common.dto.ApiResponse getWarehouseProductsForPublish() {
        List<com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct> warehouseProducts = 
                warehouseProductRepository.findAll();
        
        List<com.doan.WEB_TMDT.module.product.dto.WarehouseProductListResponse> response = 
                warehouseProducts.stream().map(wp -> {
            // Kiểm tra xem đã có Product nào liên kết chưa
            Optional<Product> existingProduct = productRepository.findAll().stream()
                    .filter(p -> p.getWarehouseProduct() != null && 
                                 p.getWarehouseProduct().getId().equals(wp.getId()))
                    .findFirst();
            
            // Lấy số lượng tồn kho từ InventoryStock
            Long stockQuantity = 0L;
            Long sellableQuantity = 0L;
            Optional<com.doan.WEB_TMDT.module.inventory.entity.InventoryStock> stockOpt = 
                    inventoryStockRepository.findByWarehouseProduct_Id(wp.getId());
            if (stockOpt.isPresent()) {
                stockQuantity = stockOpt.get().getOnHand();
                sellableQuantity = stockOpt.get().getSellable();
            }
            
            return com.doan.WEB_TMDT.module.product.dto.WarehouseProductListResponse.builder()
                    .id(wp.getId())
                    .sku(wp.getSku())
                    .internalName(wp.getInternalName())
                    .description(wp.getDescription())
                    .techSpecsJson(wp.getTechSpecsJson())
                    .lastImportDate(wp.getLastImportDate())
                    .stockQuantity(stockQuantity)
                    .sellableQuantity(sellableQuantity)
                    .supplierName(wp.getSupplier() != null ? wp.getSupplier().getName() : null)
                    .isPublished(existingProduct.isPresent())
                    .publishedProductId(existingProduct.map(Product::getId).orElse(null))
                    .build();
        }).collect(Collectors.toList());
        
        return com.doan.WEB_TMDT.common.dto.ApiResponse.success(
                "Danh sách sản phẩm trong kho", response);
    }

    @Override
    public com.doan.WEB_TMDT.common.dto.ApiResponse createProductFromWarehouse(
            com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest request) {
        
        // 1. Lấy WarehouseProduct
        var warehouseProduct = warehouseProductRepository.findById(request.getWarehouseProductId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy sản phẩm trong kho với ID: " + request.getWarehouseProductId()));

        // 2. Kiểm tra đã đăng bán chưa
        Optional<Product> existingProduct = productRepository.findAll().stream()
                .filter(p -> p.getWarehouseProduct() != null && 
                             p.getWarehouseProduct().getId().equals(warehouseProduct.getId()))
                .findFirst();
        
        if (existingProduct.isPresent()) {
            return com.doan.WEB_TMDT.common.dto.ApiResponse.error(
                    "Sản phẩm này đã được đăng bán rồi!");
        }

        // 3. Lấy Category
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // 4. Lấy số lượng tồn kho từ InventoryStock
        Long stockQuantity = 0L;
        Optional<com.doan.WEB_TMDT.module.inventory.entity.InventoryStock> stockOpt = 
                inventoryStockRepository.findByWarehouseProduct_Id(warehouseProduct.getId());
        if (stockOpt.isPresent()) {
            stockQuantity = stockOpt.get().getSellable();
        }

        // 5. Tạo Product mới
        Product product = Product.builder()
                .name(request.getName())
                .sku(warehouseProduct.getSku())
                .price(request.getPrice())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .category(category)
                .warehouseProduct(warehouseProduct)
                .stockQuantity(stockQuantity)
                .techSpecsJson(warehouseProduct.getTechSpecsJson()) // Copy thông số từ WarehouseProduct
                .build();

        // 6. Lưu Product
        Product savedProduct = productRepository.save(product);
        
        return com.doan.WEB_TMDT.common.dto.ApiResponse.success(
                "Đăng bán sản phẩm thành công", savedProduct);
    }

    @Override
    public com.doan.WEB_TMDT.common.dto.ApiResponse updatePublishedProduct(
            Long productId, 
            com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest request) {
        
        // 1. Lấy Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // 2. Lấy Category nếu thay đổi
        if (request.getCategoryId() != null) {
            var category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy danh mục với ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // 3. Cập nhật thông tin
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        // 4. Cập nhật số lượng tồn kho từ InventoryStock
        if (product.getWarehouseProduct() != null) {
            Optional<com.doan.WEB_TMDT.module.inventory.entity.InventoryStock> stockOpt = 
                    inventoryStockRepository.findByWarehouseProduct_Id(product.getWarehouseProduct().getId());
            if (stockOpt.isPresent()) {
                product.setStockQuantity(stockOpt.get().getSellable());
            }
        }

        // 5. Lưu Product
        Product updatedProduct = productRepository.save(product);
        
        return com.doan.WEB_TMDT.common.dto.ApiResponse.success(
                "Cập nhật sản phẩm thành công", updatedProduct);
    }

    @Override
    public com.doan.WEB_TMDT.common.dto.ApiResponse unpublishProduct(Long productId) {
        // 1. Lấy Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // 2. Xóa Product (gỡ khỏi trang bán)
        productRepository.delete(product);
        
        return com.doan.WEB_TMDT.common.dto.ApiResponse.success(
                "Gỡ sản phẩm khỏi trang bán thành công", null);
    }
}
