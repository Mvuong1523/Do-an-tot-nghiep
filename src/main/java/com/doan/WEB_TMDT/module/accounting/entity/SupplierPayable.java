package com.doan.WEB_TMDT.module.accounting.entity;

import com.doan.WEB_TMDT.module.inventory.entity.PurchaseOrder;
import com.doan.WEB_TMDT.module.inventory.entity.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity quản lý công nợ phải trả nhà cung cấp
 */
@Entity
@Table(name = "supplier_payables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPayable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String payableCode; // Mã công nợ (AP-YYYYMMDD-XXXX)

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount; // Tổng tiền phải trả

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount; // Đã trả

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount; // Còn nợ

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayableStatus status; // UNPAID, PARTIAL, PAID, OVERDUE

    @Column(nullable = false)
    private LocalDate invoiceDate; // Ngày hóa đơn (ngày nhập hàng)

    @Column(nullable = false)
    private LocalDate dueDate; // Ngày hạn thanh toán

    private Integer paymentTermDays; // Số ngày nợ (từ supplier)

    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (remainingAmount == null) {
            remainingAmount = totalAmount;
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Tự động cập nhật status
        updateStatus();
    }

    public void updateStatus() {
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            status = PayableStatus.PAID;
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = PayableStatus.PARTIAL;
        } else if (LocalDate.now().isAfter(dueDate)) {
            status = PayableStatus.OVERDUE;
        } else {
            status = PayableStatus.UNPAID;
        }
    }
}
