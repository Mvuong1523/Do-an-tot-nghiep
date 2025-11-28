package com.doan.WEB_TMDT.module.order.entity;

import com.doan.WEB_TMDT.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private String productName; // Lưu tên sản phẩm tại thời điểm mua
    
    @Column(nullable = false)
    private Double price; // Giá tại thời điểm mua
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double subtotal; // price * quantity
    
    private String serialNumber; // Serial nếu là sản phẩm có serial
    
    @Column(nullable = false)
    private Boolean reserved = false; // Đã giữ hàng chưa
    
    @Column(nullable = false)
    private Boolean exported = false; // Đã xuất kho chưa
}
