package com.doan.WEB_TMDT.module.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity ghi nhận các lần thanh toán cho nhà cung cấp
 */
@Entity
@Table(name = "supplier_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentCode; // Mã thanh toán (PAY-YYYYMMDD-XXXX)

    @ManyToOne
    @JoinColumn(name = "payable_id", nullable = false)
    private SupplierPayable payable;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // Số tiền thanh toán

    @Column(nullable = false)
    private LocalDate paymentDate; // Ngày thanh toán

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // CASH, BANK_TRANSFER, CHECK

    private String referenceNumber; // Số tham chiếu (số chuyển khoản, số séc...)

    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
