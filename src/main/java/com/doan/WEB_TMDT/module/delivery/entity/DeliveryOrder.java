package com.doan.WEB_TMDT.module.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_orders")
@Getter @Setter
public class DeliveryOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderCode;          // mã đơn nội bộ

    @Column(nullable = false)
    private String provider;           // "GHTK"

    private String providerLabel;      // S1.A1.XXXX
    private Long providerTrackingId;   // 2001...
    private Integer shipFee;
    private Integer insuranceFee;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = DeliveryStatus.CREATED;
    }
    @PreUpdate void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
