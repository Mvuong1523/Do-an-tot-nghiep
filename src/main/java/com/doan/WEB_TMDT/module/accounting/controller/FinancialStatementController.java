package com.doan.WEB_TMDT.module.accounting.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.service.FinancialStatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller cho báo cáo tài chính chuẩn
 * Chỉ Admin và Accountant mới có quyền truy cập
 */
@RestController
@RequestMapping("/api/accounting/financial-statement")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
public class FinancialStatementController {

    private final FinancialStatementService financialStatementService;

    /**
     * Lấy báo cáo tài chính tổng hợp
     * Validation: startDate phải trước endDate, không quá 1 năm
     */
    @GetMapping
    public ApiResponse getFinancialStatement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Validation
        if (startDate.isAfter(endDate)) {
            return ApiResponse.error("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        if (startDate.plusYears(1).isBefore(endDate)) {
            return ApiResponse.error("Khoảng thời gian không được vượt quá 1 năm");
        }
        
        return financialStatementService.getFinancialStatement(startDate, endDate);
    }

    /**
     * Lấy báo cáo doanh thu
     */
    @GetMapping("/revenue")
    public ApiResponse getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ApiResponse.error("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        return financialStatementService.getRevenueReport(startDate, endDate);
    }

    /**
     * Lấy báo cáo chi phí
     */
    @GetMapping("/expenses")
    public ApiResponse getExpenseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ApiResponse.error("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        return financialStatementService.getExpenseReport(startDate, endDate);
    }

    /**
     * Lấy báo cáo lợi nhuận
     */
    @GetMapping("/profit")
    public ApiResponse getProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ApiResponse.error("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        return financialStatementService.getProfitReport(startDate, endDate);
    }

    /**
     * Lấy báo cáo dòng tiền
     */
    @GetMapping("/cash-flow")
    public ApiResponse getCashFlowReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ApiResponse.error("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        return financialStatementService.getCashFlowReport(startDate, endDate);
    }

    /**
     * Lấy dashboard tổng quan (tháng hiện tại)
     */
    @GetMapping("/dashboard")
    public ApiResponse getDashboard() {
        return financialStatementService.getDashboard();
    }

    /**
     * Lấy báo cáo theo tháng
     */
    @GetMapping("/monthly/{year}/{month}")
    public ApiResponse getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        
        // Validation
        if (month < 1 || month > 12) {
            return ApiResponse.error("Tháng không hợp lệ (1-12)");
        }
        
        if (year < 2000 || year > LocalDate.now().getYear() + 1) {
            return ApiResponse.error("Năm không hợp lệ");
        }
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        return financialStatementService.getFinancialStatement(startDate, endDate);
    }

    /**
     * Lấy báo cáo theo quý
     */
    @GetMapping("/quarterly/{year}/{quarter}")
    public ApiResponse getQuarterlyReport(
            @PathVariable int year,
            @PathVariable int quarter) {
        
        // Validation
        if (quarter < 1 || quarter > 4) {
            return ApiResponse.error("Quý không hợp lệ (1-4)");
        }
        
        if (year < 2000 || year > LocalDate.now().getYear() + 1) {
            return ApiResponse.error("Năm không hợp lệ");
        }
        
        int startMonth = (quarter - 1) * 3 + 1;
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = startDate.plusMonths(2).withDayOfMonth(
                startDate.plusMonths(2).lengthOfMonth());
        
        return financialStatementService.getFinancialStatement(startDate, endDate);
    }

    /**
     * Lấy báo cáo theo năm
     */
    @GetMapping("/yearly/{year}")
    public ApiResponse getYearlyReport(@PathVariable int year) {
        
        // Validation
        if (year < 2000 || year > LocalDate.now().getYear() + 1) {
            return ApiResponse.error("Năm không hợp lệ");
        }
        
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        return financialStatementService.getFinancialStatement(startDate, endDate);
    }
}
