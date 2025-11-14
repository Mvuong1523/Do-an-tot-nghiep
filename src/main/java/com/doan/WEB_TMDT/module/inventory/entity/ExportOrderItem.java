package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "export_order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "export_order_id")
    private ExportOrder exportOrder;  // Phiếu xuất chứa dòng này

    @ManyToOne
    @JoinColumn(name = "warehouse_product_id")
    private WarehouseProduct warehouseProduct;

    @Column(nullable = false)
    private String sku;               // Mã sản phẩm

    @Column(nullable = false)
    private Long quantity;            // Số lượng xuất

    @Lob
    @Column(name = "serial_numbers")
    private String serialNumbers;     // Danh sách serial xuất, ví dụ "SN001,SN002,SN003"
    private Double totalCost;


}
