package com.doan.WEB_TMDT.module.accounting.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.FinancialReportRequest;
import com.doan.WEB_TMDT.module.accounting.entity.FinancialTransaction;

public interface FinancialTransactionService {
    ApiResponse getAllTransactions(int page, int size);
    ApiResponse getTransactionsByDateRange(FinancialReportRequest request);
    ApiResponse createTransaction(FinancialTransaction transaction);
    ApiResponse updateTransaction(Long id, FinancialTransaction transaction);
    ApiResponse deleteTransaction(Long id);
    ApiResponse getProfitLossReport(FinancialReportRequest request);
    ApiResponse getCashFlowReport(FinancialReportRequest request);
    ApiResponse getExpenseAnalysis(FinancialReportRequest request);
    
    // Tự động tạo transaction từ order
    void createTransactionFromOrder(String orderId);
    void createRefundTransaction(String orderId, String refundAmount);
}