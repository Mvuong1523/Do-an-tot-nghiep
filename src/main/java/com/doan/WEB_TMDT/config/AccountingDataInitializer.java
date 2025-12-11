package com.doan.WEB_TMDT.config;

import com.doan.WEB_TMDT.module.accounting.entity.*;
import com.doan.WEB_TMDT.module.accounting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Chạy sau BankAccountDataInitializer
public class AccountingDataInitializer implements CommandLineRunner {

    private final AccountingPeriodRepository periodRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final TaxReportRepository taxReportRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeAccountingPeriods();
        initializeFinancialTransactions();
        initializeTaxReports();
    }

    private void initializeAccountingPeriods() {
        if (periodRepository.count() == 0) {
            log.info("Initializing accounting periods...");

            AccountingPeriod period1 = AccountingPeriod.builder()
                    .name("Kỳ 11/2024")
                    .startDate(LocalDate.of(2024, 11, 1))
                    .endDate(LocalDate.of(2024, 11, 30))
                    .status(PeriodStatus.CLOSED)
                    .totalRevenue(BigDecimal.valueOf(50000000))
                    .totalOrders(150)
                    .discrepancyAmount(BigDecimal.valueOf(100000))
                    .discrepancyRate(0.2)
                    .closedBy("admin@company.com")
                    .closedAt(LocalDateTime.of(2024, 12, 1, 9, 0))
                    .createdAt(LocalDateTime.of(2024, 11, 1, 0, 0))
                    .build();

            AccountingPeriod period2 = AccountingPeriod.builder()
                    .name("Kỳ 12/2024")
                    .startDate(LocalDate.of(2024, 12, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .status(PeriodStatus.OPEN)
                    .totalRevenue(BigDecimal.valueOf(65000000))
                    .totalOrders(200)
                    .discrepancyAmount(BigDecimal.valueOf(50000))
                    .discrepancyRate(0.08)
                    .createdAt(LocalDateTime.now())
                    .build();

            periodRepository.save(period1);
            periodRepository.save(period2);

            log.info("Created {} accounting periods", 2);
        }
    }

    private void initializeFinancialTransactions() {
        // Không tạo dữ liệu mẫu
        // Giao dịch sẽ được tự động tạo khi có đơn hàng thanh toán thành công
        log.info("Financial transactions will be auto-generated from real orders");
    }

    private void initializeTaxReports() {
        if (taxReportRepository.count() == 0) {
            log.info("Initializing tax reports...");

            // Báo cáo VAT tháng 11 (đã nộp)
            TaxReport vatNov = TaxReport.builder()
                    .reportCode("VAT202411")
                    .periodStart(LocalDate.of(2024, 11, 1))
                    .periodEnd(LocalDate.of(2024, 11, 30))
                    .taxType(TaxType.VAT)
                    .taxableRevenue(BigDecimal.valueOf(45000000))
                    .taxRate(BigDecimal.valueOf(10.00))
                    .taxAmount(BigDecimal.valueOf(4500000))
                    .paidTax(BigDecimal.valueOf(4500000))
                    .remainingTax(BigDecimal.ZERO)
                    .status(TaxReportStatus.PAID)
                    .submittedBy("admin@company.com")
                    .submittedAt(LocalDateTime.of(2024, 12, 1, 9, 0))
                    .createdAt(LocalDateTime.of(2024, 11, 30, 16, 0))
                    .build();

            // Báo cáo VAT tháng 12 (đã nộp)
            TaxReport vatDec = TaxReport.builder()
                    .reportCode("VAT202412")
                    .periodStart(LocalDate.of(2024, 12, 1))
                    .periodEnd(LocalDate.of(2024, 12, 31))
                    .taxType(TaxType.VAT)
                    .taxableRevenue(BigDecimal.valueOf(50000000))
                    .taxRate(BigDecimal.valueOf(10.00))
                    .taxAmount(BigDecimal.valueOf(5000000))
                    .paidTax(BigDecimal.ZERO)
                    .remainingTax(BigDecimal.valueOf(5000000))
                    .status(TaxReportStatus.SUBMITTED)
                    .submittedBy("admin@company.com")
                    .submittedAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                    .createdAt(LocalDateTime.of(2024, 12, 10, 14, 0))
                    .build();

            // Báo cáo thuế TNDN quý 4 (nháp)
            TaxReport corporateQ4 = TaxReport.builder()
                    .reportCode("CIT2024Q4")
                    .periodStart(LocalDate.of(2024, 10, 1))
                    .periodEnd(LocalDate.of(2024, 12, 31))
                    .taxType(TaxType.CORPORATE_TAX)
                    .taxableRevenue(BigDecimal.valueOf(15000000))
                    .taxRate(BigDecimal.valueOf(20.00))
                    .taxAmount(BigDecimal.valueOf(3000000))
                    .paidTax(BigDecimal.ZERO)
                    .remainingTax(BigDecimal.valueOf(3000000))
                    .status(TaxReportStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();

            taxReportRepository.save(vatNov);
            taxReportRepository.save(vatDec);
            taxReportRepository.save(corporateQ4);

            log.info("Created {} tax reports", 3);
        }
    }
}