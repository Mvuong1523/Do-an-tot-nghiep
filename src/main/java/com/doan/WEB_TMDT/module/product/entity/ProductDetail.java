package com.doan.WEB_TMDT.module.product.entity;

import com.doan.WEB_TMDT.module.inventory.entity.PurchaseOrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_details",
        uniqueConstraints = @UniqueConstraint(columnNames = "serial_number"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã serial / IMEI duy nhất
    @Column(name = "serial_number", nullable = false, unique = true, length = 64)
    private String serialNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.IN_STOCK;

    @ManyToOne
    @JoinColumn(name = "po_item_id")
    private PurchaseOrderItem poItem; // dòng đơn nhập tương ứng


    private LocalDateTime warrantyUntil;

    private String note;
}
