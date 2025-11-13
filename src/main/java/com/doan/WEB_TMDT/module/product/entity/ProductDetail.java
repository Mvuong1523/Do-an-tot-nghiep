package com.doan.WEB_TMDT.module.product.entity;

import com.doan.WEB_TMDT.module.inventory.entity.ProductStatus;
import com.doan.WEB_TMDT.module.inventory.entity.PurchaseOrderItem;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 TRƯỜNG CẦN THIẾT ĐỂ FIX LỖI serialNumber
    @Column(unique = true)
    private String serialNumber;

    // 💡 TRƯỜNG DỮ LIỆU CẦN THIẾT CHO LOGIC TỒN KHO/XUẤT KHO
    private Double importPrice;
    private LocalDateTime importDate;
    private Integer warrantyMonths;

    @Enumerated(EnumType.STRING) // Cần phải có status để logic xuất kho hoạt động
    private ProductStatus status;
    private LocalDateTime soldDate; // Cần thiết cho logic xuất kho

    // 💡 LIÊN KẾT (JOIN) CẦN THIẾT CHO LOGIC CỦA BẠN
    @ManyToOne
    @JoinColumn(name = "warehouse_product_id")
    private WarehouseProduct warehouseProduct;

    @ManyToOne
    @JoinColumn(name = "purchase_order_item_id")
    private PurchaseOrderItem purchaseOrderItem;

    // --- Các trường ban đầu ---
    private String color;
    private String size;
    private String material;
    private String manufacturer;
    private String warranty;

    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product1 product1;
}