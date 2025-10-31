package com.doan.WEB_TMDT.module.inventory.entity;

import com.doan.WEB_TMDT.module.product.entity.Product;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;          // Sản phẩm xuất

    @Column(nullable = false)
    private String sku;               // Mã sản phẩm

    @Column(nullable = false)
    private Long quantity;            // Số lượng xuất

    @Lob
    @Column(name = "serial_numbers")
    private String serialNumbers;     // Danh sách serial xuất, ví dụ "SN001,SN002,SN003"

    private Long unitCost;            // Giá vốn (tuỳ chọn)
}
