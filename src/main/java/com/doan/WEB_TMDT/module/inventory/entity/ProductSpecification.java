package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specifications", indexes = {
    @Index(name = "idx_spec_key", columnList = "spec_key"),
    @Index(name = "idx_spec_value", columnList = "spec_value")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_product_id", nullable = false)
    private WarehouseProduct warehouseProduct;

    @Column(name = "spec_key", nullable = false, length = 100)
    private String specKey;   // ram, screen_size, refresh_rate, cpu...

    @Column(name = "spec_value", nullable = false, length = 255)
    private String specValue; // 8GB, 27 inch, 144Hz, Intel i7...
}
