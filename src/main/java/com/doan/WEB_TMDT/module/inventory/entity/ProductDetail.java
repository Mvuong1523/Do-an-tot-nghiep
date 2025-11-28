package com.doan.WEB_TMDT.module.inventory.entity;

import com.doan.WEB_TMDT.module.product.entity.Product;
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

    // giá nhập riêng cho từng máy (giá vốn)
    @Column(nullable = false)
    private Double importPrice;

    // giá bán thực tế (sẽ được cập nhật bởi module Order khi bán hàng)
    // Dùng để module Accounting tính lợi nhuận sau này
    private Double salePrice;

    // ngày nhập kho
    private LocalDateTime importDate;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.IN_STOCK;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_product_id")
    private WarehouseProduct warehouseProduct;

    @ManyToOne
    @JoinColumn(name = "purchase_order_item_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private PurchaseOrderItem purchaseOrderItem;


    @OneToOne(mappedBy = "productDetail", cascade = CascadeType.ALL)
    private Product product;

    private Integer warrantyMonths;      // thời hạn bảo hành (tháng)

    // đơn hàng đã bán serial này (nếu có)
    private Long soldOrderId;
    private LocalDateTime soldDate;


    private String note;
}