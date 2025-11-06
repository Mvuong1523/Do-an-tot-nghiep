package com.doan.WEB_TMDT.module.inventory.entity;

import com.doan.WEB_TMDT.module.product.entity.CatalogProduct;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inventory_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = "product_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryStock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id")
    private WarehouseProduct warehouseProduct;

    @Column(nullable = false)
    private Long onHand = 0L;     // tồn thực tế

    @Column(nullable = false)
    private Long reserved = 0L;   // đã giữ chỗ cho đơn

    @Column(nullable = false)
    private Long damaged = 0L; // sản phẩm lỗi

    // ngày kiểm kê gần nhất
    private LocalDate lastAuditDate;


    //  Tính tự động số lượng có thể bán
    @Transient
    public Long getSellable() {
        long sellable = onHand - reserved - damaged;
        return Math.max(sellable, 0L);
    }

    //  Tính tổng còn trong kho (không trừ reserved)
    @Transient
    public Long getAvailable() {
        return onHand - reserved;
    }
}
