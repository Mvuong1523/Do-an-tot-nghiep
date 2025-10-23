package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "inventory_transaction_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryTransactionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "transactionItem", cascade = CascadeType.ALL)
    private List<ProductDetail> serialDetails;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id")
    private InventoryTransaction transaction;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Long quantity;   // số lượng nhập/xuất

    private Long unitCost;   // giá vốn (khi nhập); có thể null khi xuất
}
