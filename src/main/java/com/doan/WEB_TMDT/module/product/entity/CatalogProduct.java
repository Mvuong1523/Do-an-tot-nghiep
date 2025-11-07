package com.doan.WEB_TMDT.module.product.entity;

import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalog_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;
    private String description;

    @Column(nullable = false)
    private Long sellingPrice; // giá bán

    @Column(nullable = false)
    private boolean visible = false;

    // liên kết đến SKU thực tế trong kho
    @OneToOne
    @JoinColumn(name = "warehouse_product_id", unique = true)
    private WarehouseProduct warehouseProduct;
}
