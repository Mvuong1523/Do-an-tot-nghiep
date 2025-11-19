package com.doan.WEB_TMDT.module.order.entity;

import com.doan.WEB_TMDT.module.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderCode; // Mã đơn hàng: ORD20231119001
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
    
    // Thông tin giao hàng
    @Column(nullable = false)
    private String customerName;
    
    @Column(nullable = false)
    private String customerPhone;
    
    @Column(nullable = false)
    private String customerEmail;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;
    
    private String note; // Ghi chú của khách hàng
    
    // Giá tiền
    @Column(nullable = false)
    private Double subtotal; // Tổng tiền hàng
    
    @Column(nullable = false)
    private Double shippingFee; // Phí vận chuyển
    
    @Column(nullable = false)
    private Double discount; // Giảm giá
    
    @Column(nullable = false)
    private Double total; // Tổng thanh toán
    
    // Thanh toán (chỉ lưu trạng thái, chi tiết ở Payment module)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;
    
    private Long paymentId; // Reference đến Payment entity
    
    private LocalDateTime paidAt; // Thời gian thanh toán
    
    // Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime confirmedAt; // Xác nhận đơn
    
    private LocalDateTime shippedAt; // Giao hàng
    
    private LocalDateTime deliveredAt; // Đã giao
    
    private LocalDateTime cancelledAt; // Hủy đơn
    
    private String cancelReason; // Lý do hủy
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.UNPAID;
        }
    }
}
