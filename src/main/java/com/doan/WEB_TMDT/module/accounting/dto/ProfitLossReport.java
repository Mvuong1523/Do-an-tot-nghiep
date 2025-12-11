package com.doan.WEB_TMDT.module.accounting.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ProfitLossReport {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Doanh thu
    private BigDecimal totalRevenue;
    private BigDecimal salesRevenue;
    private BigDecimal otherRevenue;
    
    // Chi phí
    private BigDecimal shippingCosts;
    private BigDecimal paymentFees;
    
    // Lợi nhuận
    private BigDecimal grossProfit;
    private BigDecimal operatingProfit;
    private BigDecimal netProfit;
    
    // Thuế
    private BigDecimal vatAmount;
    private BigDecimal corporateTax;
    
    // Tỷ suất
    private BigDecimal grossProfitMargin;
    private BigDecimal netProfitMargin;
}