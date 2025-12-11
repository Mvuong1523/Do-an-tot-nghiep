package com.doan.WEB_TMDT.module.accounting.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.TaxReportRequest;

public interface TaxService {
    ApiResponse getAllTaxReports();
    ApiResponse getTaxReportsByType(String taxType);
    ApiResponse createTaxReport(TaxReportRequest request);
    ApiResponse updateTaxReport(Long id, TaxReportRequest request);
    ApiResponse submitTaxReport(Long id);
    ApiResponse markTaxAsPaid(Long id);
    ApiResponse getTaxSummary();
    ApiResponse calculateTaxForPeriod(TaxReportRequest request);
}