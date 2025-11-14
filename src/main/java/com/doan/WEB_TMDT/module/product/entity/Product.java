package com.doan.WEB_TMDT.module.product.entity;

import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên hiển thị cho khách
    @Column(nullable = false)
    private String name;

    // Brand, model hiển thị
    private String brand;
    private String model;

    // Giá bán cho khách
    @Column(nullable = false)
    private Long salePrice;     // giá bán

    // Giá niêm yết (để hiện giảm giá % nếu muốn)
    private Long originalPrice;

    // Mô tả chi tiết
    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    // Trạng thái hiển thị
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductShowStatus status;
    // ACTIVE, HIDDEN, OUT_OF_STOCK

    // Số lượng tồn (cache)
    private Long stockQuantity;

    // Danh mục
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Liên kết với hàng trong kho
    @OneToOne
    @JoinColumn(name = "warehouse_product_id", unique = true)
    private WarehouseProduct warehouseProduct;
}
