package com.project.ecommerce.cart.entity;

import com.project.ecommerce.catalog.entity.Variant; // Cần Variant Entity
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_item")
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Liên kết với Variant để biết SKU, giá, và kiểm tra tồn kho
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(nullable = false)
    private Integer quantity;

    // Snapshot giá bán tại thời điểm thêm vào giỏ
    @Column(name = "price_snapshot", precision = 18, scale = 0)
    private BigDecimal priceSnapshot;
}