package com.doan.WEB_TMDT.module.inventory.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.util.List;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_order_id")
    @JsonBackReference
    private PurchaseOrder purchaseOrder;

    @Column(unique = true, nullable = false, length = 64)
    private String sku;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_product_id")
    private WarehouseProduct warehouseProduct;

    private Long quantity;   // số lượng đặt
    private Double unitCost;   // giá nhập
    private Integer warrantyMonths;      // thời hạn bảo hành (tháng)
    private String note;


    @OneToMany(mappedBy = "purchaseOrderItem", cascade = CascadeType.ALL)
    private List<ProductDetail> productDetails; // chứa các serial thực tế sau khi nhập
}
