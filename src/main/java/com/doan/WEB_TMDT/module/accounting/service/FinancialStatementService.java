package com.doan.WEB_TMDT.module.accounting.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;

import java.time.LocalDate;

/**
 * Service cho báo cáo tài chính chuẩn
 */
public interface FinancialStatementService {
    
    /**
     * Lấy báo cáo tài chính tổng hợp
     */
    ApiResponse getFinancialStatement(LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy báo cáo doanh thu chi tiết
     */
    ApiResponse getRevenueReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy báo cáo chi phí chi tiết
     */
    ApiResponse getExpenseReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy báo cáo lợi nhuận
     */
    ApiResponse getProfitReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy báo cáo dòng tiền
     */
    ApiResponse getCashFlowReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Lấy dashboard tổng quan
     */
    ApiResponse getDashboard();
}
