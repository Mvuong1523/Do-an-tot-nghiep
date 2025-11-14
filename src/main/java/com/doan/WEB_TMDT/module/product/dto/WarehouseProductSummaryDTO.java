package com.doan.WEB_TMDT.module.product.dto;

import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WarehouseProductSummaryDTO {

    private Long id;
    private String internalName;
    private String sku;
    private String brand;
    private String description;
    private Long quantityInStock;

    public static WarehouseProductSummaryDTO fromEntity(WarehouseProduct wp) {
        long qty = wp.getQuantityInStock(); // dùng @Transient trong WarehouseProduct

        return WarehouseProductSummaryDTO.builder()
                .id(wp.getId())
                .internalName(wp.getInternalName())
                .sku(wp.getSku())
                .brand(wp.getBrand())
                .description(wp.getDescription())
                .quantityInStock(qty)
                .build();
    }
}
