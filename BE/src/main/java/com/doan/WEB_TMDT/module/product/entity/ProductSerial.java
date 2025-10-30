package com.doan.WEB_TMDT.module.product.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_serials", uniqueConstraints = @UniqueConstraint(columnNames = {"serial"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSerial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, unique = true)
    private String serial;

    @Enumerated(EnumType.STRING)
    private SerialStatus status; // AVAILABLE, RESERVED, SOLD, RETURNED, DAMAGED

    private String location;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
