package com.doan.WEB_TMDT.module.accounting.dto;

import com.doan.WEB_TMDT.module.accounting.entity.PayableStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPayableResponse {
    private Long id;
    private String payableCode;
    private Long supplierId;
    private String supplierName;
    private String supplierTaxCode;
    private Long purchaseOrderId;
    private String purchaseOrderCode;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private PayableStatus status;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private Integer paymentTermDays;
    private Integer daysOverdue; // Số ngày quá hạn (nếu có)
    private String note;
}
