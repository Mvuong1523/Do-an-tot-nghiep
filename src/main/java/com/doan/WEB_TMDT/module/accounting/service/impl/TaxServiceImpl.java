package com.doan.WEB_TMDT.module.accounting.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.util.SecurityUtils;
import com.doan.WEB_TMDT.module.accounting.dto.TaxReportRequest;
import com.doan.WEB_TMDT.module.accounting.entity.*;
import com.doan.WEB_TMDT.module.accounting.repository.FinancialTransactionRepository;
import com.doan.WEB_TMDT.module.accounting.repository.TaxReportRepository;
import com.doan.WEB_TMDT.module.accounting.service.TaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {

    private final TaxReportRepository taxReportRepository;
    private final FinancialTransactionRepository transactionRepository;

    @Override
    public ApiResponse getAllTaxReports() {
        List<TaxReport> reports = taxReportRepository.findAll();
        return ApiResponse.success("Danh sách báo cáo thuế", reports);
    }

    @Override
    public ApiResponse getTaxReportsByType(String taxType) {
        try {
            TaxType type = TaxType.valueOf(taxType.toUpperCase());
            List<TaxReport> reports = taxReportRepository.findByTaxTypeOrderByPeriodStartDesc(type);
            return ApiResponse.success("Báo cáo thuế " + taxType, reports);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Loại thuế không hợp lệ");
        }
    }

    @Override
    @Transactional
    public ApiResponse createTaxReport(TaxReportRequest request) {
        // Kiểm tra xem đã có báo cáo cho kỳ này chưa
        Optional<TaxReport> existing = taxReportRepository.findByTaxTypeAndPeriodStartAndPeriodEnd(
                request.getTaxType(), request.getPeriodStart(), request.getPeriodEnd());
        
        if (existing.isPresent()) {
            return ApiResponse.error("Đã có báo cáo thuế cho kỳ này");
        }

        // Tính toán số liệu thuế
        Map<String, BigDecimal> taxCalculation = calculateTaxAmounts(request);

        String reportCode = generateReportCode(request.getTaxType(), request.getPeriodStart());
        
        TaxReport report = TaxReport.builder()
                .reportCode(reportCode)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .taxType(request.getTaxType())
                .taxableRevenue(taxCalculation.get("taxableRevenue"))
                .taxRate(request.getTaxRate())
                .taxAmount(taxCalculation.get("taxAmount"))
                .paidTax(BigDecimal.ZERO)
                .remainingTax(taxCalculation.get("taxAmount"))
                .status(TaxReportStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        TaxReport saved = taxReportRepository.save(report);
        return ApiResponse.success("Tạo báo cáo thuế thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse updateTaxReport(Long id, TaxReportRequest request) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));

        if (report.getStatus() != TaxReportStatus.DRAFT) {
            return ApiResponse.error("Chỉ có thể sửa báo cáo ở trạng thái nháp");
        }

        // Tính toán lại số liệu thuế
        Map<String, BigDecimal> taxCalculation = calculateTaxAmounts(request);

        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setTaxRate(request.getTaxRate());
        report.setTaxableRevenue(taxCalculation.get("taxableRevenue"));
        report.setTaxAmount(taxCalculation.get("taxAmount"));
        report.setRemainingTax(taxCalculation.get("taxAmount").subtract(report.getPaidTax()));
        report.setUpdatedAt(LocalDateTime.now());

        TaxReport updated = taxReportRepository.save(report);
        return ApiResponse.success("Cập nhật báo cáo thuế thành công", updated);
    }

    @Override
    @Transactional
    public ApiResponse submitTaxReport(Long id) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));

        if (report.getStatus() != TaxReportStatus.DRAFT) {
            return ApiResponse.error("Chỉ có thể nộp báo cáo ở trạng thái nháp");
        }

        String currentUser = SecurityUtils.getCurrentUserEmail();
        
        report.setStatus(TaxReportStatus.SUBMITTED);
        report.setSubmittedBy(currentUser != null ? currentUser : "System");
        report.setSubmittedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        TaxReport updated = taxReportRepository.save(report);
        return ApiResponse.success("Nộp báo cáo thuế thành công", updated);
    }

    @Override
    @Transactional
    public ApiResponse markTaxAsPaid(Long id) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));

        if (report.getStatus() != TaxReportStatus.SUBMITTED) {
            return ApiResponse.error("Chỉ có thể thanh toán báo cáo đã nộp");
        }

        report.setStatus(TaxReportStatus.PAID);
        report.setPaidTax(report.getTaxAmount());
        report.setRemainingTax(BigDecimal.ZERO);
        report.setUpdatedAt(LocalDateTime.now());

        TaxReport updated = taxReportRepository.save(report);
        return ApiResponse.success("Đánh dấu đã thanh toán thuế thành công", updated);
    }

    @Override
    public ApiResponse getTaxSummary() {
        Map<String, Object> summary = new HashMap<>();

        // VAT Summary
        BigDecimal vatOwed = taxReportRepository.sumRemainingTaxByType(TaxType.VAT);
        BigDecimal vatPaid = taxReportRepository.sumTaxAmountByTypeAndStatus(TaxType.VAT, TaxReportStatus.PAID);

        // Corporate Tax Summary
        BigDecimal corporateOwed = taxReportRepository.sumRemainingTaxByType(TaxType.CORPORATE_TAX);
        BigDecimal corporatePaid = taxReportRepository.sumTaxAmountByTypeAndStatus(TaxType.CORPORATE_TAX, TaxReportStatus.PAID);

        summary.put("vatOwed", vatOwed != null ? vatOwed : BigDecimal.ZERO);
        summary.put("vatPaid", vatPaid != null ? vatPaid : BigDecimal.ZERO);
        summary.put("corporateOwed", corporateOwed != null ? corporateOwed : BigDecimal.ZERO);
        summary.put("corporatePaid", corporatePaid != null ? corporatePaid : BigDecimal.ZERO);
        summary.put("totalOwed", 
                (vatOwed != null ? vatOwed : BigDecimal.ZERO)
                .add(corporateOwed != null ? corporateOwed : BigDecimal.ZERO));

        return ApiResponse.success("Tổng quan thuế", summary);
    }

    @Override
    public ApiResponse calculateTaxForPeriod(TaxReportRequest request) {
        Map<String, BigDecimal> calculation = calculateTaxAmounts(request);
        return ApiResponse.success("Tính toán thuế cho kỳ", calculation);
    }

    private Map<String, BigDecimal> calculateTaxAmounts(TaxReportRequest request) {
        LocalDateTime startDateTime = request.getPeriodStart().atStartOfDay();
        LocalDateTime endDateTime = request.getPeriodEnd().atTime(23, 59, 59);

        Map<String, BigDecimal> result = new HashMap<>();

        if (request.getTaxType() == TaxType.VAT) {
            // Tính VAT từ doanh thu bán hàng
            BigDecimal salesRevenue = transactionRepository.sumAmountByCategoryAndDateRange(
                    TransactionCategory.SALES, startDateTime, endDateTime);
            
            if (salesRevenue == null) salesRevenue = BigDecimal.ZERO;
            
            // Doanh thu chịu thuế VAT (loại trừ VAT)
            BigDecimal taxableRevenue = salesRevenue.divide(
                    BigDecimal.ONE.add(request.getTaxRate().divide(BigDecimal.valueOf(100))), 
                    2, RoundingMode.HALF_UP);
            
            BigDecimal vatAmount = taxableRevenue.multiply(request.getTaxRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            result.put("taxableRevenue", taxableRevenue);
            result.put("taxAmount", vatAmount);
            
        } else if (request.getTaxType() == TaxType.CORPORATE_TAX) {
            // Tính thuế TNDN từ lợi nhuận
            BigDecimal totalRevenue = transactionRepository.sumAmountByTypeAndDateRange(
                    TransactionType.REVENUE, startDateTime, endDateTime);
            BigDecimal totalExpense = transactionRepository.sumAmountByTypeAndDateRange(
                    TransactionType.EXPENSE, startDateTime, endDateTime);
            
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
            if (totalExpense == null) totalExpense = BigDecimal.ZERO;
            
            BigDecimal profit = totalRevenue.subtract(totalExpense);
            BigDecimal corporateTax = profit.multiply(request.getTaxRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            result.put("taxableRevenue", profit);
            result.put("taxAmount", corporateTax.max(BigDecimal.ZERO));
        }

        return result;
    }

    private String generateReportCode(TaxType taxType, java.time.LocalDate periodStart) {
        String prefix = taxType == TaxType.VAT ? "VAT" : "CIT";
        String yearMonth = periodStart.getYear() + String.format("%02d", periodStart.getMonthValue());
        return prefix + yearMonth + System.currentTimeMillis() % 1000;
    }
}