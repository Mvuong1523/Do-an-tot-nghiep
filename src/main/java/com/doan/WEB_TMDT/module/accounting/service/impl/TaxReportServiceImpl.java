package com.doan.WEB_TMDT.module.accounting.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.TaxReportRequest;
import com.doan.WEB_TMDT.module.accounting.dto.TaxReportResponse;
import com.doan.WEB_TMDT.module.accounting.dto.TaxSummaryResponse;
import com.doan.WEB_TMDT.module.accounting.entity.TaxReport;
import com.doan.WEB_TMDT.module.accounting.entity.TaxStatus;
import com.doan.WEB_TMDT.module.accounting.entity.TaxType;
import com.doan.WEB_TMDT.module.accounting.repository.TaxReportRepository;
import com.doan.WEB_TMDT.module.accounting.service.TaxReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxReportServiceImpl implements TaxReportService {

    private final TaxReportRepository taxReportRepository;

    @Override
    public ApiResponse getAllTaxReports() {
        List<TaxReport> reports = taxReportRepository.findAllByOrderByPeriodStartDesc();
        List<TaxReportResponse> response = reports.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách báo cáo thuế thành công", response);
    }

    @Override
    public ApiResponse getTaxReportsByType(TaxType taxType) {
        List<TaxReport> reports = taxReportRepository.findByTaxType(taxType);
        List<TaxReportResponse> response = reports.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách báo cáo thuế thành công", response);
    }

    @Override
    public ApiResponse getTaxReportById(Long id) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));
        return ApiResponse.success("Lấy thông tin báo cáo thuế thành công", toResponse(report));
    }

    @Override
    @Transactional
    public ApiResponse createTaxReport(TaxReportRequest request, String createdBy) {
        LocalDate periodStart = LocalDate.parse(request.getPeriodStart());
        LocalDate periodEnd = LocalDate.parse(request.getPeriodEnd());

        // Calculate tax amount
        Double taxAmount = request.getTaxableRevenue() * (request.getTaxRate() / 100);

        TaxReport report = TaxReport.builder()
                .taxType(request.getTaxType())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .taxableRevenue(request.getTaxableRevenue())
                .taxRate(request.getTaxRate())
                .taxAmount(taxAmount)
                .paidAmount(0.0)
                .remainingTax(taxAmount)
                .status(TaxStatus.DRAFT)
                .createdBy(createdBy)
                .build();

        TaxReport saved = taxReportRepository.save(report);
        return ApiResponse.success("Tạo báo cáo thuế thành công", toResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse updateTaxReport(Long id, TaxReportRequest request) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));

        if (report.getStatus() != TaxStatus.DRAFT) {
            return ApiResponse.error("Chỉ có thể sửa báo cáo ở trạng thái nháp");
        }

        LocalDate periodStart = LocalDate.parse(request.getPeriodStart());
        LocalDate periodEnd = LocalDate.parse(request.getPeriodEnd());
        Double taxAmount = request.getTaxableRevenue() * (request.getTaxRate() / 100);

        report.setTaxType(request.getTaxType());
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setTaxableRevenue(request.getTaxableRevenue());
        report.setTaxRate(request.getTaxRate());
        report.setTaxAmount(taxAmount);
        report.setRemainingTax(taxAmount - (report.getPaidAmount() != null ? report.getPaidAmount() : 0.0));

        TaxReport updated = taxReportRepository.save(report);
        return ApiResponse.success("Cập nhật báo cáo thuế thành công", toResponse(updated));
    }

    @Override
    @Transactional
    public ApiResponse submitTaxReport(Long id) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));

        if (report.getStatus() != TaxStatus.DRAFT) {
            return ApiResponse.error("Báo cáo này đã được gửi");
        }

        report.setStatus(TaxStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());

        TaxReport updated = taxReportRepository.save(report);
        return ApiResponse.success("Gửi báo cáo thuế thành công", toResponse(updated));
    }

    @Override
    @Transactional
    public ApiResponse markAsPaid(Long id) {
        TaxReport report = taxReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo thuế"));

        if (report.getStatus() == TaxStatus.PAID) {
            return ApiResponse.error("Báo cáo này đã được đánh dấu thanh toán");
        }

        report.setStatus(TaxStatus.PAID);
        report.setPaidAmount(report.getTaxAmount());
        report.setRemainingTax(0.0);
        report.setPaidAt(LocalDateTime.now());

        TaxReport updated = taxReportRepository.save(report);
        return ApiResponse.success("Đánh dấu đã thanh toán thành công", toResponse(updated));
    }

    @Override
    public ApiResponse getTaxSummary() {
        Double vatOwed = taxReportRepository.sumRemainingTaxByType(TaxType.VAT);
        Double vatPaid = taxReportRepository.sumPaidAmountByType(TaxType.VAT);
        Double corporateOwed = taxReportRepository.sumRemainingTaxByType(TaxType.CORPORATE_TAX);
        Double corporatePaid = taxReportRepository.sumPaidAmountByType(TaxType.CORPORATE_TAX);

        TaxSummaryResponse summary = TaxSummaryResponse.builder()
                .vatOwed(vatOwed != null ? vatOwed : 0.0)
                .vatPaid(vatPaid != null ? vatPaid : 0.0)
                .corporateOwed(corporateOwed != null ? corporateOwed : 0.0)
                .corporatePaid(corporatePaid != null ? corporatePaid : 0.0)
                .totalOwed((vatOwed != null ? vatOwed : 0.0) + (corporateOwed != null ? corporateOwed : 0.0))
                .totalPaid((vatPaid != null ? vatPaid : 0.0) + (corporatePaid != null ? corporatePaid : 0.0))
                .build();

        return ApiResponse.success("Lấy tổng quan thuế thành công", summary);
    }

    private TaxReportResponse toResponse(TaxReport report) {
        return TaxReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .taxType(report.getTaxType())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .taxableRevenue(report.getTaxableRevenue())
                .taxRate(report.getTaxRate())
                .taxAmount(report.getTaxAmount())
                .paidAmount(report.getPaidAmount())
                .remainingTax(report.getRemainingTax())
                .status(report.getStatus())
                .submittedAt(report.getSubmittedAt())
                .paidAt(report.getPaidAt())
                .createdAt(report.getCreatedAt())
                .createdBy(report.getCreatedBy())
                .build();
    }
}
