package com.doan.WEB_TMDT.module.accounting.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Báo cáo tài chính chuẩn (Financial Statement)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatementResponse {
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    // I. DOANH THU (Revenue)
    private RevenueSection revenue;
    
    // II. CHI PHÍ (Expenses)
    private ExpenseSection expenses;
    
    // III. LỢI NHUẬN (Profit)
    private ProfitSection profit;
    
    // IV. CÔNG NỢ (Payables)
    private PayableSection payables;
    
    // V. TIỀN MẶT (Cash Flow)
    private CashFlowSection cashFlow;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSection {
        private BigDecimal totalRevenue;           // Tổng doanh thu
        private BigDecimal productRevenue;         // Doanh thu bán hàng
        private BigDecimal shippingRevenue;        // Doanh thu vận chuyển
        private BigDecimal otherRevenue;           // Doanh thu khác
        private Integer orderCount;                // Số đơn hàng
        private List<RevenueDetail> details;       // Chi tiết theo ngày/tháng
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSection {
        private BigDecimal totalExpense;           // Tổng chi phí
        private BigDecimal costOfGoodsSold;        // Giá vốn hàng bán (COGS)
        private BigDecimal shippingExpense;        // Chi phí vận chuyển
        private BigDecimal paymentFee;             // Phí thanh toán
        private BigDecimal operatingExpense;       // Chi phí hoạt động
        private BigDecimal otherExpense;           // Chi phí khác
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitSection {
        private BigDecimal grossProfit;            // Lợi nhuận gộp = Doanh thu - Giá vốn
        private BigDecimal operatingProfit;        // Lợi nhuận hoạt động = Lợi nhuận gộp - Chi phí HĐ
        private BigDecimal netProfit;              // Lợi nhuận ròng = Lợi nhuận HĐ - Thuế
        private BigDecimal profitMargin;           // Tỷ suất lợi nhuận (%)
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayableSection {
        private BigDecimal totalPayable;           // Tổng công nợ phải trả
        private BigDecimal paidAmount;             // Đã thanh toán
        private BigDecimal remainingAmount;        // Còn nợ
        private Integer overdueCount;              // Số công nợ quá hạn
        private BigDecimal overdueAmount;          // Số tiền quá hạn
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashFlowSection {
        private BigDecimal cashIn;                 // Tiền vào (từ khách hàng)
        private BigDecimal cashOut;                // Tiền ra (trả NCC, chi phí)
        private BigDecimal netCashFlow;            // Dòng tiền ròng
        private BigDecimal beginningBalance;       // Số dư đầu kỳ
        private BigDecimal endingBalance;          // Số dư cuối kỳ
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDetail {
        private LocalDate date;
        private BigDecimal amount;
        private Integer orderCount;
    }
}
