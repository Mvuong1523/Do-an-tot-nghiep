package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    private String brand;
    private String category;
    private String unit;     // vd: cái, hộp

    @Column(nullable = false)
    private Long price;      // giá bán niêm yết (đồng)

    @Column(nullable = false)
    private Boolean active = true;
}
