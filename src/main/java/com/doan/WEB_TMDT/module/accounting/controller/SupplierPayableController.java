package com.doan.WEB_TMDT.module.accounting.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.CreatePaymentRequest;
import com.doan.WEB_TMDT.module.accounting.service.SupplierPayableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounting/payables")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT', 'WAREHOUSE_MANAGER')")
public class SupplierPayableController {

    private final SupplierPayableService payableService;

    /**
     * Lấy tất cả công nợ
     */
    @GetMapping
    public ApiResponse getAllPayables() {
        return payableService.getAllPayables();
    }

    /**
     * Lấy công nợ theo ID
     */
    @GetMapping("/{id}")
    public ApiResponse getPayableById(@PathVariable Long id) {
        return payableService.getPayableById(id);
    }

    /**
     * Lấy công nợ theo nhà cung cấp
     */
    @GetMapping("/supplier/{supplierId}")
    public ApiResponse getPayablesBySupplier(@PathVariable Long supplierId) {
        return payableService.getPayablesBySupplier(supplierId);
    }

    /**
     * Lấy công nợ quá hạn
     */
    @GetMapping("/overdue")
    public ApiResponse getOverduePayables() {
        return payableService.getOverduePayables();
    }

    /**
     * Lấy công nợ sắp đến hạn
     */
    @GetMapping("/upcoming")
    public ApiResponse getUpcomingPayables(@RequestParam(defaultValue = "7") Integer days) {
        return payableService.getUpcomingPayables(days);
    }

    /**
     * Thanh toán công nợ
     */
    @PostMapping("/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
    public ApiResponse makePayment(@Valid @RequestBody CreatePaymentRequest request) {
        return payableService.makePayment(request);
    }

    /**
     * Lấy lịch sử thanh toán
     */
    @GetMapping("/{payableId}/payments")
    public ApiResponse getPaymentHistory(@PathVariable Long payableId) {
        return payableService.getPaymentHistory(payableId);
    }

    /**
     * Thống kê công nợ
     */
    @GetMapping("/stats")
    public ApiResponse getPayableStats() {
        return payableService.getPayableStats();
    }

    /**
     * Báo cáo công nợ theo khoảng thời gian
     */
    @GetMapping("/report")
    public ApiResponse getPayableReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return payableService.getPayableReport(startDate, endDate);
    }
}
