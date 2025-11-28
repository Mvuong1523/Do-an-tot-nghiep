package com.doan.WEB_TMDT.module.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reconciliation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReconciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String gateway; // VNPAY, MOMO, ZALOPAY

    @Column(nullable = false)
    private BigDecimal systemAmount;

    @Column(nullable = false)
    private BigDecimal gatewayAmount;

    @Column(nullable = false)
    private BigDecimal discrepancy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status; // MATCHED, MISMATCHED, MISSING_IN_SYSTEM, MISSING_IN_GATEWAY

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private LocalDateTime reconciledAt;

    private String reconciledBy;

    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
