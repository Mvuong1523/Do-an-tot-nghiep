package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inventory_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, unique = true, length = 64)
    private String code; // mã phiếu nhập/xuất

    @ManyToOne
    @JoinColumn(name = "supplier_id") // null khi xuất kho
    private Supplier supplier;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String createdBy; // email/username

    private String note;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryTransactionItem> items;
}
