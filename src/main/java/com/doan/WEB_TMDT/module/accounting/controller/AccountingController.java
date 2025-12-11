package com.doan.WEB_TMDT.module.accounting.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.FinancialReportRequest;
import com.doan.WEB_TMDT.module.accounting.dto.ReconciliationRequest;
import com.doan.WEB_TMDT.module.accounting.dto.TaxReportRequest;
import com.doan.WEB_TMDT.module.accounting.entity.FinancialTransaction;
import com.doan.WEB_TMDT.module.accounting.service.AccountingService;
import com.doan.WEB_TMDT.module.accounting.service.FinancialTransactionService;
import com.doan.WEB_TMDT.module.accounting.service.TaxService;
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
    private final TaxService taxService;
    private final FinancialTransactionService transactionService;

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
    
    @GetMapping("/shipping-reconciliation/export")
    public ApiResponse exportShippingReconciliation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return accountingService.exportShippingReconciliation(startDate, endDate);
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

    // Tax Management
    @GetMapping("/tax/reports")
    public ApiResponse getAllTaxReports() {
        return taxService.getAllTaxReports();
    }

    @GetMapping("/tax/reports/{taxType}")
    public ApiResponse getTaxReportsByType(@PathVariable String taxType) {
        return taxService.getTaxReportsByType(taxType);
    }

    @PostMapping("/tax/reports")
    public ApiResponse createTaxReport(@RequestBody TaxReportRequest request) {
        return taxService.createTaxReport(request);
    }

    @PutMapping("/tax/reports/{id}")
    public ApiResponse updateTaxReport(@PathVariable Long id, @RequestBody TaxReportRequest request) {
        return taxService.updateTaxReport(id, request);
    }

    @PostMapping("/tax/reports/{id}/submit")
    public ApiResponse submitTaxReport(@PathVariable Long id) {
        return taxService.submitTaxReport(id);
    }

    @PostMapping("/tax/reports/{id}/mark-paid")
    public ApiResponse markTaxAsPaid(@PathVariable Long id) {
        return taxService.markTaxAsPaid(id);
    }

    @GetMapping("/tax/summary")
    public ApiResponse getTaxSummary() {
        return taxService.getTaxSummary();
    }

    @PostMapping("/tax/calculate")
    public ApiResponse calculateTaxForPeriod(@RequestBody TaxReportRequest request) {
        return taxService.calculateTaxForPeriod(request);
    }

    // Financial Transactions
    @GetMapping("/transactions")
    public ApiResponse getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return transactionService.getAllTransactions(page, size);
    }

    @PostMapping("/transactions/search")
    public ApiResponse getTransactionsByDateRange(@RequestBody FinancialReportRequest request) {
        return transactionService.getTransactionsByDateRange(request);
    }

    @PostMapping("/transactions")
    public ApiResponse createTransaction(@RequestBody FinancialTransaction transaction) {
        return transactionService.createTransaction(transaction);
    }

    @PutMapping("/transactions/{id}")
    public ApiResponse updateTransaction(@PathVariable Long id, @RequestBody FinancialTransaction transaction) {
        return transactionService.updateTransaction(id, transaction);
    }

    @DeleteMapping("/transactions/{id}")
    public ApiResponse deleteTransaction(@PathVariable Long id) {
        return transactionService.deleteTransaction(id);
    }

    // Advanced Reports
    @PostMapping("/reports/profit-loss")
    public ApiResponse getProfitLossReport(@RequestBody FinancialReportRequest request) {
        return transactionService.getProfitLossReport(request);
    }

    @PostMapping("/reports/cash-flow")
    public ApiResponse getCashFlowReport(@RequestBody FinancialReportRequest request) {
        return transactionService.getCashFlowReport(request);
    }

    @PostMapping("/reports/expense-analysis")
    public ApiResponse getExpenseAnalysis(@RequestBody FinancialReportRequest request) {
        return transactionService.getExpenseAnalysis(request);
    }
}
