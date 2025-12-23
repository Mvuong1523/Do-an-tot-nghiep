package com.doan.WEB_TMDT.module.accounting.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.TaxReportRequest;
import com.doan.WEB_TMDT.module.accounting.entity.TaxType;

public interface TaxReportService {
    ApiResponse getAllTaxReports();
    ApiResponse getTaxReportsByType(TaxType taxType);
    ApiResponse getTaxReportById(Long id);
    ApiResponse createTaxReport(TaxReportRequest request, String createdBy);
    ApiResponse updateTaxReport(Long id, TaxReportRequest request);
    ApiResponse submitTaxReport(Long id);
    ApiResponse markAsPaid(Long id);
    ApiResponse getTaxSummary();
}
