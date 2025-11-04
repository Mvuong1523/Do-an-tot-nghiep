package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="warehouse_product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url; // hoặc path sau khi upload

    @ManyToOne
    @JoinColumn(name="warehouse_product_id")
    private WarehouseProduct warehouseProduct;
}
