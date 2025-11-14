package com.doan.WEB_TMDT.module.cart.entity;

import com.doan.WEB_TMDT.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giỏ hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Sản phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Snapshot vài thông tin để hiển thị nhanh (optional nhưng tiện)
    private String productName;
    private String productImage;

    @Column(nullable = false)
    private Integer quantity;

    // Đơn giá tại thời điểm add vào giỏ
    @Column(nullable = false)
    private Long unitPrice;

    // Tổng tiền dòng
    @Column(nullable = false)
    private Long lineTotal;
}
