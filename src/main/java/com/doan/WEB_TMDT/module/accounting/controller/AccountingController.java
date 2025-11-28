package com.doan.WEB_TMDT.module.accounting.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.ReconciliationRequest;
import com.doan.WEB_TMDT.module.accounting.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
public class AccountingController {

    private final AccountingService accountingService;

    // Dashboard stats
    @GetMapping("/stats")
    public ApiResponse getStats() {
        return accountingService.getStats();
    }

    // Payment reconciliation
    @PostMapping("/payment-reconciliation")
    public ApiResponse getPaymentReconciliation(@RequestBody ReconciliationRequest request) {
        return accountingService.getPaymentReconciliation(request);
    }

    @PostMapping("/payment-reconciliation/import")
    public ApiResponse importReconciliationFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("gateway") String gateway) {
        return accountingService.importReconciliationFile(file, gateway);
    }

    // Shipping reconciliation
    @GetMapping("/shipping-reconciliation")
    public ApiResponse getShippingReconciliation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return accountingService.getShippingReconciliation(startDate, endDate);
    }

    // Financial reports
    @GetMapping("/reports")
    public ApiResponse getFinancialReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ORDERS") String viewMode) {
        return accountingService.getFinancialReports(startDate, endDate, viewMode);
    }

    @GetMapping("/reports/export")
    public ApiResponse exportReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return accountingService.exportReports(startDate, endDate);
    }

    // Period closing
    @GetMapping("/periods")
    public ApiResponse getAllPeriods() {
        return accountingService.getAllPeriods();
    }

    @PostMapping("/periods/{id}/close")
    public ApiResponse closePeriod(@PathVariable Long id) {
        return accountingService.closePeriod(id);
    }

    @PostMapping("/periods/{id}/reopen")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse reopenPeriod(@PathVariable Long id) {
        return accountingService.reopenPeriod(id);
    }
}
