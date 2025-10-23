package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = "product_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryStock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Long onHand = 0L;     // tồn thực tế

    @Column(nullable = false)
    private Long reserved = 0L;   // đã giữ chỗ cho đơn

    @Transient
    public Long getAvailable() {
        long a = onHand - reserved;
        return a < 0 ? 0 : a;
    }
}
