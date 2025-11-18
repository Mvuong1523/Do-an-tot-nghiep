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
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();

        // Lấy specifications từ WarehouseProduct
        if (product.getWarehouseProduct() != null && 
            product.getWarehouseProduct().getSpecifications() != null) {
            
            Map<String, String> specs = product.getWarehouseProduct()
                    .getSpecifications()
                    .stream()
                    .collect(Collectors.toMap(
                            ProductSpecification::getSpecKey,
                            ProductSpecification::getSpecValue
                    ));
            dto.setSpecifications(specs);
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
}
