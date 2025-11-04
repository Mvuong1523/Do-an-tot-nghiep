package com.doan.WEB_TMDT.module.inventory.entity;

import com.doan.WEB_TMDT.module.product.entity.CatalogProduct;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "warehouse_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã SKU duy nhất trong kho
    @Column(unique = true, nullable = false, length = 64)
    private String sku;

    @Column(nullable = false)
    private String internalName; // tên kỹ thuật

    @Column(columnDefinition = "TEXT")
    private String techSpecsJson;        // thông số kỹ thuật (JSON)

    @Column(columnDefinition = "TEXT")
    private String description;          // mô tả

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private LocalDateTime lastImportDate;



    // Liên kết sang sản phẩm hiển thị
    @OneToOne(mappedBy = "warehouseProduct", cascade = CascadeType.ALL)
    private CatalogProduct catalogProduct;

    // Danh sách serial chi tiết
    @OneToMany(mappedBy = "warehouseProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetail> serials;

    // ảnh đính kèm (nhiều ảnh)
    @OneToMany(mappedBy="warehouseProduct", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<WarehouseProductImage> images;

    @Transient
    public long getQuantityInStock() {
        return serials == null ? 0L :
                serials.stream().filter(s -> s.getStatus() == ProductStatus.IN_STOCK).count();
    }
}
