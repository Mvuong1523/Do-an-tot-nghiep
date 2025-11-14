package com.doan.WEB_TMDT.module.product.dto;

import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductShowStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductDTO {

    private Long id;

    private String name;
    private String brand;
    private String model;

    private Long salePrice;
    private Long originalPrice;

    private String description;
    private String imageUrl;

    private ProductShowStatus status;
    private Long stockQuantity;

    private Long categoryId;
    private String categoryName;

    private Long warehouseProductId;

    public static ProductDTO fromEntity(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .brand(p.getBrand())
                .model(p.getModel())
                .salePrice(p.getSalePrice())
                .originalPrice(p.getOriginalPrice())
                .description(p.getDescription())
                .imageUrl(p.getImageUrl())
                .status(p.getStatus())
                .stockQuantity(p.getStockQuantity())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .warehouseProductId(
                        p.getWarehouseProduct() != null ? p.getWarehouseProduct().getId() : null
                )
                .build();
    }
}
