package com.doan.WEB_TMDT.module.accounting.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FinancialReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType; // PROFIT_LOSS, CASH_FLOW, BALANCE_SHEET
    private String groupBy; // DAILY, MONTHLY, QUARTERLY
}