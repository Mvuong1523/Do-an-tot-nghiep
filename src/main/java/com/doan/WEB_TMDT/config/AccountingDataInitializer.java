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
        if (transactionRepository.count() == 0) {
            log.info("Initializing financial transactions...");

            // Doanh thu từ bán hàng
            FinancialTransaction revenue1 = FinancialTransaction.builder()
                    .transactionCode("TXN001")
                    .orderId("ORD001")
                    .type(TransactionType.REVENUE)
                    .category(TransactionCategory.SALES)
                    .amount(BigDecimal.valueOf(1500000))
                    .description("Doanh thu bán hàng đơn ORD001")
                    .transactionDate(LocalDateTime.of(2024, 12, 1, 10, 0))
                    .createdBy("System")
                    .createdAt(LocalDateTime.now())
                    .build();

            // Chi phí vận chuyển
            FinancialTransaction shipping1 = FinancialTransaction.builder()
                    .transactionCode("TXN002")
                    .orderId("ORD001")
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.SHIPPING)
                    .amount(BigDecimal.valueOf(50000))
                    .description("Phí vận chuyển đơn ORD001")
                    .transactionDate(LocalDateTime.of(2024, 12, 1, 10, 0))
                    .createdBy("System")
                    .createdAt(LocalDateTime.now())
                    .build();

            // Chi phí thanh toán
            FinancialTransaction payment1 = FinancialTransaction.builder()
                    .transactionCode("TXN003")
                    .orderId("ORD001")
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.PAYMENT_FEE)
                    .amount(BigDecimal.valueOf(30000))
                    .description("Phí thanh toán đơn ORD001")
                    .transactionDate(LocalDateTime.of(2024, 12, 1, 10, 0))
                    .createdBy("System")
                    .createdAt(LocalDateTime.now())
                    .build();

            // Doanh thu đơn hàng thứ 2
            FinancialTransaction revenue2 = FinancialTransaction.builder()
                    .transactionCode("TXN004")
                    .orderId("ORD002")
                    .type(TransactionType.REVENUE)
                    .category(TransactionCategory.SALES)
                    .amount(BigDecimal.valueOf(2500000))
                    .description("Doanh thu bán hàng đơn ORD002")
                    .transactionDate(LocalDateTime.of(2024, 12, 2, 14, 30))
                    .createdBy("System")
                    .createdAt(LocalDateTime.now())
                    .build();

            // Chi phí marketing
            FinancialTransaction marketing = FinancialTransaction.builder()
                    .transactionCode("TXN005")
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.MARKETING)
                    .amount(BigDecimal.valueOf(5000000))
                    .description("Chi phí quảng cáo Facebook tháng 12")
                    .transactionDate(LocalDateTime.of(2024, 12, 1, 0, 0))
                    .createdBy("admin@company.com")
                    .createdAt(LocalDateTime.now())
                    .build();

            // Chi phí vận hành
            FinancialTransaction operational = FinancialTransaction.builder()
                    .transactionCode("TXN006")
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.OPERATIONAL)
                    .amount(BigDecimal.valueOf(3000000))
                    .description("Tiền thuê văn phòng tháng 12")
                    .transactionDate(LocalDateTime.of(2024, 12, 1, 0, 0))
                    .createdBy("admin@company.com")
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(revenue1);
            transactionRepository.save(shipping1);
            transactionRepository.save(payment1);
            transactionRepository.save(revenue2);
            transactionRepository.save(marketing);
            transactionRepository.save(operational);

            log.info("Created {} financial transactions", 6);
        }
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