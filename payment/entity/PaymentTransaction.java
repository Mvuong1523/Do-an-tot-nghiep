package com.doan.WEB_TMDT.module.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String paymentCode;

    private Long orderId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String qrBase64;
    private String bankReference;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    @Lob
    private String rawPayload;
}
