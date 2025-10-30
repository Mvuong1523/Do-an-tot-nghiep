package com.doan.WEB_TMDT.module.order.entity;

import com.doan.WEB_TMDT.module.inventory.entity.Product;
import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "serial_id")
    private ProductDetail productDetail;  // mỗi item tương ứng 1 sản phẩm có IMEI/Serial

    private Long price;
}
