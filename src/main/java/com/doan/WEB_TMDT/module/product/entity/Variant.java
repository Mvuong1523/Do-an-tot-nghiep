package com.project.ecommerce.catalog.entity;

import java.math.BigDecimal;

@Entity
@Table(name = "variant")
@Data
public class Variant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;
    @Column(name = "attributes")
    private String attributes;
    @Column(name = "selling_price", precision = 18, scale = 0)
    private BigDecimal sellingPrice;
    @Column(name = "current_stock")
    private Integer currentStock = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}