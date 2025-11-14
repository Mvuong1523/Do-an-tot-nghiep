package com.doan.WEB_TMDT.module.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // userId từ hệ thống auth (không map entity để đỡ phụ thuộc)
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // lưu tổng tiền để xem nhanh (có thể tính lại từ items nếu muốn)
    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Integer totalItems;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = CartStatus.ACTIVE;
        }
        if (this.totalAmount == null) {
            this.totalAmount = 0L;
        }
        if (this.totalItems == null) {
            this.totalItems = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
