package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

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
    public com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO toProductWithSpecs(Product product) {
        var dto = com.doan.WEB_TMDT.module.product.dto.ProductWithSpecsDTO.builder()
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
            
            java.util.Map<String, String> specs = product.getWarehouseProduct()
                    .getSpecifications()
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            com.doan.WEB_TMDT.module.inventory.entity.ProductSpecification::getSpecKey,
                            com.doan.WEB_TMDT.module.inventory.entity.ProductSpecification::getSpecValue
                    ));
            dto.setSpecifications(specs);
        }

        return dto;
    }
}
