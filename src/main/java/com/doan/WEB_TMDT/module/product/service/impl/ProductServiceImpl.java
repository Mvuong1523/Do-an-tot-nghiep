package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import com.doan.WEB_TMDT.module.inventory.repository.WarehouseProductRepository;
import com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest;
import com.doan.WEB_TMDT.module.product.dto.UpdateProductRequest;
import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductShowStatus;
import com.doan.WEB_TMDT.module.product.repository.CategoryRepository;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createFromWarehouse(CreateProductFromWarehouseRequest req) {
        WarehouseProduct wp = warehouseProductRepository.findById(req.getWarehouseProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy WarehouseProduct"));

        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Category"));
        }

        long stock = wp.getQuantityInStock(); // từ entity WarehouseProduct

        Product product = Product.builder()
                .name(req.getName() != null ? req.getName() : wp.getInternalName())
                .brand(req.getBrand() != null ? req.getBrand() : wp.getBrand())
                .model(req.getModel())
                .salePrice(req.getSalePrice())
                .originalPrice(req.getOriginalPrice())
                .description(req.getDescription() != null ? req.getDescription() : wp.getDescription())
                .imageUrl(req.getImageUrl())
                .category(category)
                .warehouseProduct(wp)
                .stockQuantity(stock)
                .status(stock > 0 ? ProductShowStatus.ACTIVE : ProductShowStatus.OUT_OF_STOCK)
                .build();

        return productRepository.save(product);
    }

    @Override
    public Product updatePrice(Long id, Long newPrice) {
        Product product = findByIdOrThrow(id);
        product.setSalePrice(newPrice);
        return productRepository.save(product);
    }

    @Override
    public Product updateBasicInfo(Long id, UpdateProductRequest req) {
        Product product = findByIdOrThrow(id);

        if (req.getName() != null) product.setName(req.getName());
        if (req.getBrand() != null) product.setBrand(req.getBrand());
        if (req.getModel() != null) product.setModel(req.getModel());
        if (req.getSalePrice() != null) product.setSalePrice(req.getSalePrice());
        if (req.getOriginalPrice() != null) product.setOriginalPrice(req.getOriginalPrice());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getImageUrl() != null) product.setImageUrl(req.getImageUrl());

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Category"));
            product.setCategory(category);
        }

        if (req.getStatus() != null) {
            product.setStatus(req.getStatus());
        }

        return productRepository.save(product);
    }

    @Override
    public void hide(Long id) {
        Product product = findByIdOrThrow(id);
        product.setStatus(ProductShowStatus.HIDDEN);
        productRepository.save(product);
    }

    @Override
    public Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Product với id = " + id));
    }

    @Override
    public void syncStockFromWarehouse(Product product) {
        WarehouseProduct wp = product.getWarehouseProduct();
        if (wp == null) return;

        long stock = wp.getQuantityInStock();
        product.setStockQuantity(stock);

        if (stock <= 0 && product.getStatus() == ProductShowStatus.ACTIVE) {
            product.setStatus(ProductShowStatus.OUT_OF_STOCK);
        } else if (stock > 0 && product.getStatus() == ProductShowStatus.OUT_OF_STOCK) {
            product.setStatus(ProductShowStatus.ACTIVE);
        }

        productRepository.save(product);
    }
}
