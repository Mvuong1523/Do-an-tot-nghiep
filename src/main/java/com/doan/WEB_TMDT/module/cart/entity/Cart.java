package com.project.ecommerce.cart.entity;

import com.project.ecommerce.auth.entity.User; // Giả định
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cart")
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết 1-1 với Khách hàng (User/ThanhVien)
    // Cart được tạo ra khi User đăng ký thành công hoặc lần đầu đăng nhập
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Các món hàng trong giỏ (CartItem)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    private Integer totalItems = 0; // Tổng số lượng mặt hàng

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}