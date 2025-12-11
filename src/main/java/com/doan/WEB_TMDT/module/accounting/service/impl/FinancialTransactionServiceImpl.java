package com.doan.WEB_TMDT.module.accounting.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.util.SecurityUtils;
import com.doan.WEB_TMDT.module.accounting.dto.FinancialReportRequest;
import com.doan.WEB_TMDT.module.accounting.dto.ProfitLossReport;
import com.doan.WEB_TMDT.module.accounting.entity.*;
import com.doan.WEB_TMDT.module.accounting.repository.FinancialTransactionRepository;
import com.doan.WEB_TMDT.module.accounting.service.FinancialTransactionService;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinancialTransactionServiceImpl implements FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    @Override
    public ApiResponse getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<FinancialTransaction> transactions = transactionRepository.findAll(pageable);
        return ApiResponse.success("Danh sách giao dịch tài chính", transactions);
    }

    @Override
    public ApiResponse getTransactionsByDateRange(FinancialReportRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);
        
        List<FinancialTransaction> transactions = transactionRepository
                .findByTransactionDateBetween(startDateTime, endDateTime);
        
        return ApiResponse.success("Giao dịch trong khoảng thời gian", transactions);
    }

    @Override
    @Transactional
    public ApiResponse createTransaction(FinancialTransaction transaction) {
        String currentUser = SecurityUtils.getCurrentUserEmail();
        
        transaction.setTransactionCode(generateTransactionCode());
        transaction.setCreatedBy(currentUser != null ? currentUser : "System");
        transaction.setCreatedAt(LocalDateTime.now());
        
        FinancialTransaction saved = transactionRepository.save(transaction);
        return ApiResponse.success("Tạo giao dịch thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse updateTransaction(Long id, FinancialTransaction transaction) {
        FinancialTransaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
        
        existing.setType(transaction.getType());
        existing.setCategory(transaction.getCategory());
        existing.setAmount(transaction.getAmount());
        existing.setDescription(transaction.getDescription());
        existing.setTransactionDate(transaction.getTransactionDate());
        existing.setUpdatedAt(LocalDateTime.now());
        
        FinancialTransaction updated = transactionRepository.save(existing);
        return ApiResponse.success("Cập nhật giao dịch thành công", updated);
    }

    @Override
    @Transactional
    public ApiResponse deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            return ApiResponse.error("Không tìm thấy giao dịch");
        }
        
        transactionRepository.deleteById(id);
        return ApiResponse.success("Xóa giao dịch thành công", null);
    }

    @Override
    public ApiResponse getProfitLossReport(FinancialReportRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        // Tính doanh thu
        BigDecimal salesRevenue = getAmountByCategory(TransactionCategory.SALES, startDateTime, endDateTime);
        BigDecimal totalRevenue = getAmountByType(TransactionType.REVENUE, startDateTime, endDateTime);
        BigDecimal otherRevenue = totalRevenue.subtract(salesRevenue);

        // Tính chi phí thực tế từ hệ thống
        BigDecimal shippingCosts = getAmountByCategory(TransactionCategory.SHIPPING, startDateTime, endDateTime);
        BigDecimal paymentFees = getAmountByCategory(TransactionCategory.PAYMENT_FEE, startDateTime, endDateTime);
        BigDecimal taxAmount = getAmountByCategory(TransactionCategory.TAX, startDateTime, endDateTime);

        // Tính lợi nhuận
        BigDecimal totalExpenses = shippingCosts.add(paymentFees);
        BigDecimal grossProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal netProfit = grossProfit.subtract(taxAmount);

        // Tính tỷ suất
        BigDecimal grossProfitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 
                ? grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal netProfitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        ProfitLossReport report = ProfitLossReport.builder()
                .period(request.getStartDate() + " - " + request.getEndDate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalRevenue(totalRevenue)
                .salesRevenue(salesRevenue)
                .otherRevenue(otherRevenue)
                .shippingCosts(shippingCosts)
                .paymentFees(paymentFees)
                .grossProfit(grossProfit)
                .netProfit(netProfit)
                .vatAmount(taxAmount)
                .grossProfitMargin(grossProfitMargin)
                .netProfitMargin(netProfitMargin)
                .build();

        return ApiResponse.success("Báo cáo lãi lỗ", report);
    }

    @Override
    public ApiResponse getCashFlowReport(FinancialReportRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        Map<String, Object> cashFlow = new HashMap<>();
        
        // Dòng tiền từ hoạt động kinh doanh
        BigDecimal operatingCashIn = getAmountByType(TransactionType.REVENUE, startDateTime, endDateTime);
        BigDecimal operatingCashOut = getAmountByType(TransactionType.EXPENSE, startDateTime, endDateTime);
        BigDecimal netOperatingCash = operatingCashIn.subtract(operatingCashOut);

        // Dòng tiền từ hoạt động đầu tư (giả định = 0 cho e-commerce)
        BigDecimal investingCashFlow = BigDecimal.ZERO;

        // Dòng tiền từ hoạt động tài chính (giả định = 0)
        BigDecimal financingCashFlow = BigDecimal.ZERO;

        // Dòng tiền ròng
        BigDecimal netCashFlow = netOperatingCash.add(investingCashFlow).add(financingCashFlow);

        cashFlow.put("period", request.getStartDate() + " - " + request.getEndDate());
        cashFlow.put("operatingCashIn", operatingCashIn);
        cashFlow.put("operatingCashOut", operatingCashOut);
        cashFlow.put("netOperatingCash", netOperatingCash);
        cashFlow.put("investingCashFlow", investingCashFlow);
        cashFlow.put("financingCashFlow", financingCashFlow);
        cashFlow.put("netCashFlow", netCashFlow);

        return ApiResponse.success("Báo cáo dòng tiền", cashFlow);
    }

    @Override
    public ApiResponse getExpenseAnalysis(FinancialReportRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<Object[]> expenseByCategory = transactionRepository
                .sumAmountByTypeAndCategoryAndDateRange(TransactionType.EXPENSE, startDateTime, endDateTime);

        List<Map<String, Object>> analysis = new ArrayList<>();
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Object[] row : expenseByCategory) {
            TransactionCategory category = (TransactionCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totalExpense = totalExpense.add(amount);
        }

        for (Object[] row : expenseByCategory) {
            TransactionCategory category = (TransactionCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            Map<String, Object> item = new HashMap<>();
            item.put("category", category.name());
            item.put("amount", amount);
            item.put("percentage", percentage);
            analysis.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("period", request.getStartDate() + " - " + request.getEndDate());
        result.put("totalExpense", totalExpense);
        result.put("breakdown", analysis);

        return ApiResponse.success("Phân tích chi phí", result);
    }

    @Override
    @Transactional
    public void createTransactionFromOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByOrderCode(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }

        Order order = orderOpt.get();
        
        // Tạo giao dịch doanh thu
        FinancialTransaction revenueTransaction = FinancialTransaction.builder()
                .transactionCode(generateTransactionCode())
                .orderId(orderId)
                .type(TransactionType.REVENUE)
                .category(TransactionCategory.SALES)
                .amount(BigDecimal.valueOf(order.getTotal()))
                .description("Doanh thu từ đơn hàng " + orderId)
                .transactionDate(order.getCreatedAt())
                .createdBy("System")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(revenueTransaction);

        // Tạo giao dịch phí vận chuyển
        if (order.getShippingFee() > 0) {
            FinancialTransaction shippingTransaction = FinancialTransaction.builder()
                    .transactionCode(generateTransactionCode())
                    .orderId(orderId)
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.SHIPPING)
                    .amount(BigDecimal.valueOf(order.getShippingFee()))
                    .description("Phí vận chuyển đơn hàng " + orderId)
                    .transactionDate(order.getCreatedAt())
                    .createdBy("System")
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(shippingTransaction);
        }

        // Tạo giao dịch phí thanh toán (2% doanh thu)
        BigDecimal paymentFee = BigDecimal.valueOf(order.getTotal()).multiply(BigDecimal.valueOf(0.02));
        FinancialTransaction paymentTransaction = FinancialTransaction.builder()
                .transactionCode(generateTransactionCode())
                .orderId(orderId)
                .type(TransactionType.EXPENSE)
                .category(TransactionCategory.PAYMENT_FEE)
                .amount(paymentFee)
                .description("Phí thanh toán đơn hàng " + orderId)
                .transactionDate(order.getCreatedAt())
                .createdBy("System")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(paymentTransaction);
    }

    @Override
    @Transactional
    public void createRefundTransaction(String orderId, String refundAmount) {
        FinancialTransaction refundTransaction = FinancialTransaction.builder()
                .transactionCode(generateTransactionCode())
                .orderId(orderId)
                .type(TransactionType.REFUND)
                .category(TransactionCategory.SALES)
                .amount(new BigDecimal(refundAmount))
                .description("Hoàn tiền đơn hàng " + orderId)
                .transactionDate(LocalDateTime.now())
                .createdBy("System")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(refundTransaction);
    }

    private BigDecimal getAmountByType(TransactionType type, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal amount = transactionRepository.sumAmountByTypeAndDateRange(type, startDate, endDate);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private BigDecimal getAmountByCategory(TransactionCategory category, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal amount = transactionRepository.sumAmountByCategoryAndDateRange(category, startDate, endDate);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis();
    }
}