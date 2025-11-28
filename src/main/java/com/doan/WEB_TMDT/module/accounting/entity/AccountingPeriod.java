package com.doan.WEB_TMDT.module.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_period")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodStatus status; // OPEN, CLOSED

    private BigDecimal totalRevenue;

    private Integer totalOrders;

    private BigDecimal discrepancyAmount;

    private Double discrepancyRate;

    private String closedBy;

    private LocalDateTime closedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
