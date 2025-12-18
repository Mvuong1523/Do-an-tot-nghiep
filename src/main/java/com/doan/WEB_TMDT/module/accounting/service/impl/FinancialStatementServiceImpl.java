package com.doan.WEB_TMDT.module.accounting.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.FinancialStatementResponse;
import com.doan.WEB_TMDT.module.accounting.entity.TransactionType;
import com.doan.WEB_TMDT.module.accounting.repository.FinancialTransactionRepository;
import com.doan.WEB_TMDT.module.accounting.repository.SupplierPayableRepository;
import com.doan.WEB_TMDT.module.accounting.repository.SupplierPaymentRepository;
import com.doan.WEB_TMDT.module.accounting.service.FinancialStatementService;
import com.doan.WEB_TMDT.module.order.entity.OrderStatus;
import com.doan.WEB_TMDT.module.order.entity.PaymentStatus;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import com.doan.WEB_TMDT.module.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialStatementServiceImpl implements FinancialStatementService {

    private final FinancialTransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SupplierPayableRepository payableRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;

    @Override
    public ApiResponse getFinancialStatement(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);

            // I. DOANH THU
            FinancialStatementResponse.RevenueSection revenue = calculateRevenue(start, end);

            // II. CHI PHÍ
            FinancialStatementResponse.ExpenseSection expenses = calculateExpenses(start, end);

            // III. LỢI NHUẬN
            FinancialStatementResponse.ProfitSection profit = calculateProfit(revenue, expenses);

            // IV. CÔNG NỢ
            FinancialStatementResponse.PayableSection payables = calculatePayables(startDate, endDate);

            // V. DÒNG TIỀN
            FinancialStatementResponse.CashFlowSection cashFlow = calculateCashFlow(start, end);

            FinancialStatementResponse statement = FinancialStatementResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .revenue(revenue)
                    .expenses(expenses)
                    .profit(profit)
                    .payables(payables)
                    .cashFlow(cashFlow)
                    .build();

            return ApiResponse.success("Báo cáo tài chính", statement);
        } catch (Exception e) {
            log.error("Error generating financial statement", e);
            return ApiResponse.error("Lỗi khi tạo báo cáo tài chính: " + e.getMessage());
        }
    }

    private FinancialStatementResponse.RevenueSection calculateRevenue(LocalDateTime start, LocalDateTime end) {
        // Lấy tất cả đơn hàng đã xác nhận và đã thanh toán trong kỳ
        var orders = orderRepository.findByCreatedAtBetween(start, end).stream()
                .filter(o -> (o.getStatus() == OrderStatus.CONFIRMED || 
                             o.getStatus() == OrderStatus.PROCESSING ||
                             o.getStatus() == OrderStatus.SHIPPING ||
                             o.getStatus() == OrderStatus.DELIVERED ||
                             o.getStatus() == OrderStatus.COMPLETED) &&
                            o.getPaymentStatus() == PaymentStatus.PAID)
                .toList();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal productRevenue = BigDecimal.ZERO;
        BigDecimal shippingRevenue = BigDecimal.ZERO;

        for (var order : orders) {
            BigDecimal subtotal = BigDecimal.valueOf(order.getSubtotal());
            BigDecimal shipping = BigDecimal.valueOf(order.getShippingFee());
            
            productRevenue = productRevenue.add(subtotal);
            shippingRevenue = shippingRevenue.add(shipping);
            totalRevenue = totalRevenue.add(subtotal).add(shipping);
        }

        return FinancialStatementResponse.RevenueSection.builder()
                .totalRevenue(totalRevenue)
                .productRevenue(productRevenue)
                .shippingRevenue(shippingRevenue)
                .otherRevenue(BigDecimal.ZERO)
                .orderCount(orders.size())
                .build();
    }

    private FinancialStatementResponse.ExpenseSection calculateExpenses(LocalDateTime start, LocalDateTime end) {
        var transactions = transactionRepository.findByTransactionDateBetween(start, end);

        BigDecimal costOfGoodsSold = BigDecimal.ZERO;
        BigDecimal shippingExpense = BigDecimal.ZERO;
        BigDecimal paymentFee = BigDecimal.ZERO;
        BigDecimal otherExpense = BigDecimal.ZERO;

        for (var tx : transactions) {
            if (tx.getType() == TransactionType.EXPENSE) {
                switch (tx.getCategory()) {
                    case COST_OF_GOODS:
                        costOfGoodsSold = costOfGoodsSold.add(tx.getAmount());
                        break;
                    case SHIPPING:
                        shippingExpense = shippingExpense.add(tx.getAmount());
                        break;
                    case PAYMENT_FEE:
                        paymentFee = paymentFee.add(tx.getAmount());
                        break;
                    default:
                        otherExpense = otherExpense.add(tx.getAmount());
                        break;
                }
            }
        }

        // Thêm chi phí trả NCC
        BigDecimal supplierPayments = supplierPaymentRepository
                .getTotalPaymentInPeriod(start.toLocalDate(), end.toLocalDate());
        if (supplierPayments != null) {
            costOfGoodsSold = costOfGoodsSold.add(supplierPayments);
        }

        BigDecimal totalExpense = costOfGoodsSold.add(shippingExpense)
                .add(paymentFee).add(otherExpense);

        return FinancialStatementResponse.ExpenseSection.builder()
                .totalExpense(totalExpense)
                .costOfGoodsSold(costOfGoodsSold)
                .shippingExpense(shippingExpense)
                .paymentFee(paymentFee)
                .operatingExpense(BigDecimal.ZERO)
                .otherExpense(otherExpense)
                .build();
    }

    private FinancialStatementResponse.ProfitSection calculateProfit(
            FinancialStatementResponse.RevenueSection revenue,
            FinancialStatementResponse.ExpenseSection expenses) {

        BigDecimal grossProfit = revenue.getTotalRevenue().subtract(expenses.getCostOfGoodsSold());
        BigDecimal operatingProfit = grossProfit.subtract(expenses.getOperatingExpense())
                .subtract(expenses.getOtherExpense());
        BigDecimal netProfit = operatingProfit.subtract(expenses.getPaymentFee())
                .subtract(expenses.getShippingExpense());

        BigDecimal profitMargin = BigDecimal.ZERO;
        if (revenue.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = netProfit.divide(revenue.getTotalRevenue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return FinancialStatementResponse.ProfitSection.builder()
                .grossProfit(grossProfit)
                .operatingProfit(operatingProfit)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .build();
    }

    private FinancialStatementResponse.PayableSection calculatePayables(LocalDate startDate, LocalDate endDate) {
        var payables = payableRepository.findByInvoiceDateBetween(startDate, endDate);

        BigDecimal totalPayable = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = BigDecimal.ZERO;
        int overdueCount = 0;
        BigDecimal overdueAmount = BigDecimal.ZERO;

        for (var payable : payables) {
            totalPayable = totalPayable.add(payable.getTotalAmount());
            paidAmount = paidAmount.add(payable.getPaidAmount());
            remainingAmount = remainingAmount.add(payable.getRemainingAmount());

            if (payable.getDueDate().isBefore(LocalDate.now()) && 
                payable.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
                overdueCount++;
                overdueAmount = overdueAmount.add(payable.getRemainingAmount());
            }
        }

        return FinancialStatementResponse.PayableSection.builder()
                .totalPayable(totalPayable)
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .overdueCount(overdueCount)
                .overdueAmount(overdueAmount)
                .build();
    }

    private FinancialStatementResponse.CashFlowSection calculateCashFlow(LocalDateTime start, LocalDateTime end) {
        // Tiền vào: Từ khách hàng (đơn hàng đã thanh toán)
        BigDecimal cashIn = paymentRepository.findByPaidAtBetween(start, end).stream()
                .map(p -> BigDecimal.valueOf(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tiền ra: Trả NCC
        BigDecimal cashOut = supplierPaymentRepository
                .getTotalPaymentInPeriod(start.toLocalDate(), end.toLocalDate());
        if (cashOut == null) {
            cashOut = BigDecimal.ZERO;
        }

        BigDecimal netCashFlow = cashIn.subtract(cashOut);

        return FinancialStatementResponse.CashFlowSection.builder()
                .cashIn(cashIn)
                .cashOut(cashOut)
                .netCashFlow(netCashFlow)
                .beginningBalance(BigDecimal.ZERO) // TODO: Implement
                .endingBalance(netCashFlow) // TODO: Implement properly
                .build();
    }

    @Override
    public ApiResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        FinancialStatementResponse.RevenueSection revenue = calculateRevenue(start, end);
        return ApiResponse.success("Báo cáo doanh thu", revenue);
    }

    @Override
    public ApiResponse getExpenseReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        FinancialStatementResponse.ExpenseSection expenses = calculateExpenses(start, end);
        return ApiResponse.success("Báo cáo chi phí", expenses);
    }

    @Override
    public ApiResponse getProfitReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        FinancialStatementResponse.RevenueSection revenue = calculateRevenue(start, end);
        FinancialStatementResponse.ExpenseSection expenses = calculateExpenses(start, end);
        FinancialStatementResponse.ProfitSection profit = calculateProfit(revenue, expenses);
        return ApiResponse.success("Báo cáo lợi nhuận", profit);
    }

    @Override
    public ApiResponse getCashFlowReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        FinancialStatementResponse.CashFlowSection cashFlow = calculateCashFlow(start, end);
        return ApiResponse.success("Báo cáo dòng tiền", cashFlow);
    }

    @Override
    public ApiResponse getDashboard() {
        // Dashboard cho tháng hiện tại
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        return getFinancialStatement(startOfMonth, endOfMonth);
    }
}
