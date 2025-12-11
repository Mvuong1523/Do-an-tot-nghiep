package com.doan.WEB_TMDT.module.accounting.dto;

import com.doan.WEB_TMDT.module.accounting.entity.TaxType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TaxReportRequest {
    private TaxType taxType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal taxRate;
}