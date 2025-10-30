package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String poCode; // Mã PO duy nhất

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private LocalDateTime orderDate;    // Ngày đặt hàng
    private LocalDateTime receivedDate; // Ngày nhập thực tế (nếu có)

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status; // CREATED, RECEIVED, CANCELED

    private String createdBy;  // email quản lý kho
    private String note;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    private List<PurchaseOrderItem> items;
}
