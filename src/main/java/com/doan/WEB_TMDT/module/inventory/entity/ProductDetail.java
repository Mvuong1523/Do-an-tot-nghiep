package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_details", uniqueConstraints = @UniqueConstraint(columnNames = "serial_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // serial/IMEI duy nhất
    @Column(name = "serial_number", nullable = false, unique = true, length = 64)
    private String serialNumber;

    // giá nhập riêng cho từng máy
    @Column(nullable = false)
    private Double importPrice;

    // ngày nhập kho
    private LocalDateTime importDate;


    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.IN_STOCK;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_product_id")
    private WarehouseProduct warehouseProduct;

    @ManyToOne
    @JoinColumn(name = "purchase_order_item_id")
    private PurchaseOrderItem purchaseOrderItem;


    private Integer warrantyMonths;      // thời hạn bảo hành (tháng)

    // đơn hàng đã bán serial này (nếu có)
    private Long soldOrderId;
    private LocalDateTime soldDate;


    private String note;
}
