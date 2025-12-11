package com.doan.WEB_TMDT.module.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reportCode;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxType taxType; // VAT, CORPORATE_TAX

    // Doanh thu chịu thuế
    @Column(nullable = false)
    private BigDecimal taxableRevenue;

    // Thuế suất (%)
    @Column(nullable = false)
    private BigDecimal taxRate;

    // Số thuế phải nộp
    @Column(nullable = false)
    private BigDecimal taxAmount;

    // Thuế đã nộp
    private BigDecimal paidTax;

    // Thuế còn phải nộp
    private BigDecimal remainingTax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxReportStatus status; // DRAFT, SUBMITTED, PAID

    private String submittedBy;
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}