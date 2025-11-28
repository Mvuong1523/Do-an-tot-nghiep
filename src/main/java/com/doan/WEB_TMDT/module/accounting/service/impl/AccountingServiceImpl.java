package com.doan.WEB_TMDT.module.accounting.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.util.SecurityUtils;
import com.doan.WEB_TMDT.module.accounting.dto.ReconciliationRequest;
import com.doan.WEB_TMDT.module.accounting.entity.*;
import com.doan.WEB_TMDT.module.accounting.repository.*;
import com.doan.WEB_TMDT.module.accounting.service.AccountingService;
import com.doan.WEB_TMDT.module.accounting.service.ExcelExportService;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AccountingServiceImpl implements AccountingService {

    private final PaymentReconciliationRepository reconciliationRepo;
    private final AccountingPeriodRepository periodRepo;
    private final OrderRepository orderRepo;
    private final ExcelExportService excelExportService;

    @Override
    public ApiResponse getStats() {
        long pendingCount = reconciliationRepo.countByStatus(ReconciliationStatus.MISMATCHED);
        long completedCount = reconciliationRepo.countByStatus(ReconciliationStatus.MATCHED);
        Double discrepancyAmount = reconciliationRepo.sumDiscrepancyByStatus(ReconciliationStatus.MISMATCHED);

        // Get real revenue from orders (last 30 days)
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        Double totalRevenue = orderRepo.sumTotalByDateRange(startDate, endDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        stats.put("pendingReconciliation", pendingCount);
        stats.put("completedReconciliation", completedCount);
        stats.put("discrepancies", pendingCount);
        stats.put("discrepancyAmount", discrepancyAmount != null ? discrepancyAmount : 0);

        return ApiResponse.success("Thống kê kế toán", stats);
    }

    @Override
    @Transactional
    public ApiResponse getPaymentReconciliation(ReconciliationRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<PaymentReconciliation> reconciliations;
        
        if ("ALL".equals(request.getGateway())) {
            reconciliations = reconciliationRepo.findByTransactionDateBetween(startDateTime, endDateTime);
        } else {
            reconciliations = reconciliationRepo.findByGatewayAndTransactionDateBetween(
                    request.getGateway(), startDateTime, endDateTime);
        }

        // Calculate summary
        Map<String, Object> result = new HashMap<>();
        result.put("data", reconciliations);
        result.put("summary", calculateSummary(reconciliations));

        return ApiResponse.success("Dữ liệu đối soát", result);
    }

    @Override
    @Transactional
    public ApiResponse importReconciliationFile(MultipartFile file, String gateway) {
        try {
            List<PaymentReconciliation> reconciliations = new ArrayList<>();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                String[] values = line.split(",");
                if (values.length < 4) continue;
                
                String orderCode = values[0].trim();
                String transactionId = values[1].trim();
                BigDecimal gatewayAmount = new BigDecimal(values[2].trim());
                LocalDateTime transactionDate = LocalDateTime.parse(values[3].trim());
                
                // Query system amount from orders
                BigDecimal systemAmount = BigDecimal.ZERO;
                Optional<Order> orderOpt = orderRepo.findByOrderCode(orderCode);
                if (orderOpt.isPresent()) {
                    systemAmount = BigDecimal.valueOf(orderOpt.get().getTotal());
                }
                
                BigDecimal discrepancy = systemAmount.subtract(gatewayAmount).abs();
                
                ReconciliationStatus status;
                if (systemAmount.compareTo(BigDecimal.ZERO) == 0) {
                    status = ReconciliationStatus.MISSING_IN_SYSTEM;
                } else if (discrepancy.compareTo(BigDecimal.ZERO) == 0) {
                    status = ReconciliationStatus.MATCHED;
                } else {
                    status = ReconciliationStatus.MISMATCHED;
                }
                
                PaymentReconciliation reconciliation = PaymentReconciliation.builder()
                        .orderId(orderCode)
                        .transactionId(transactionId)
                        .gateway(gateway)
                        .systemAmount(systemAmount)
                        .gatewayAmount(gatewayAmount)
                        .discrepancy(discrepancy)
                        .status(status)
                        .transactionDate(transactionDate)
                        .createdAt(LocalDateTime.now())
                        .build();
                
                reconciliations.add(reconciliation);
            }
            
            reconciliationRepo.saveAll(reconciliations);
            
            return ApiResponse.success("Import thành công " + reconciliations.size() + " giao dịch", reconciliations);
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi import file: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse getShippingReconciliation(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement shipping reconciliation logic
        Map<String, Object> result = new HashMap<>();
        result.put("totalShippingCost", 5000000);
        result.put("paidToCarrier", 4500000);
        result.put("collectedFromCustomer", 5200000);
        result.put("discrepancy", 200000);
        
        return ApiResponse.success("Đối soát vận chuyển", result);
    }

    @Override
    public ApiResponse getFinancialReports(LocalDate startDate, LocalDate endDate, String viewMode) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Order> orders = orderRepo.findPaidOrdersBetween(startDateTime, endDateTime);
        List<Map<String, Object>> reports = new ArrayList<>();
        
        if ("ORDERS".equals(viewMode)) {
            // Chi tiết từng đơn hàng
            for (Order order : orders) {
                Map<String, Object> report = calculateOrderFinancials(order);
                reports.add(report);
            }
        } else {
            // Tổng hợp theo ngày/tháng
            Map<String, List<Order>> groupedOrders = groupOrdersByPeriod(orders, viewMode);
            for (Map.Entry<String, List<Order>> entry : groupedOrders.entrySet()) {
                Map<String, Object> report = calculatePeriodFinancials(entry.getKey(), entry.getValue());
                reports.add(report);
            }
        }
        
        return ApiResponse.success("Báo cáo tài chính", reports);
    }
    
    private Map<String, Object> calculateOrderFinancials(Order order) {
        Map<String, Object> report = new HashMap<>();
        
        double revenue = order.getTotal();
        double vat = revenue * 0.1; // VAT 10%
        double costOfGoods = order.getSubtotal() * 0.6; // Giả định giá vốn 60%
        double shippingCost = order.getShippingFee();
        double paymentGatewayCost = revenue * 0.02; // Phí cổng thanh toán 2%
        
        double grossProfit = revenue - vat - costOfGoods - shippingCost - paymentGatewayCost;
        double corporateTax = grossProfit * 0.2; // Thuế TNDN 20%
        double netProfit = grossProfit - corporateTax;
        double actualReceived = revenue - paymentGatewayCost;
        
        report.put("orderId", order.getOrderCode());
        report.put("date", order.getCreatedAt().toLocalDate().toString());
        report.put("revenue", Math.round(revenue));
        report.put("vat", Math.round(vat));
        report.put("costOfGoods", Math.round(costOfGoods));
        report.put("shippingCost", Math.round(shippingCost));
        report.put("paymentGatewayCost", Math.round(paymentGatewayCost));
        report.put("grossProfit", Math.round(grossProfit));
        report.put("corporateTax", Math.round(corporateTax));
        report.put("netProfit", Math.round(netProfit));
        report.put("actualReceived", Math.round(actualReceived));
        
        return report;
    }
    
    private Map<String, List<Order>> groupOrdersByPeriod(List<Order> orders, String viewMode) {
        Map<String, List<Order>> grouped = new LinkedHashMap<>();
        
        for (Order order : orders) {
            String key;
            if ("DAILY".equals(viewMode)) {
                key = order.getCreatedAt().toLocalDate().toString();
            } else {
                key = order.getCreatedAt().getYear() + "-" + 
                      String.format("%02d", order.getCreatedAt().getMonthValue());
            }
            
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(order);
        }
        
        return grouped;
    }
    
    private Map<String, Object> calculatePeriodFinancials(String period, List<Order> orders) {
        Map<String, Object> report = new HashMap<>();
        
        double totalRevenue = 0;
        double totalVat = 0;
        double totalCostOfGoods = 0;
        double totalShippingCost = 0;
        double totalPaymentGatewayCost = 0;
        
        for (Order order : orders) {
            double revenue = order.getTotal();
            totalRevenue += revenue;
            totalVat += revenue * 0.1;
            totalCostOfGoods += order.getSubtotal() * 0.6;
            totalShippingCost += order.getShippingFee();
            totalPaymentGatewayCost += revenue * 0.02;
        }
        
        double grossProfit = totalRevenue - totalVat - totalCostOfGoods - totalShippingCost - totalPaymentGatewayCost;
        double corporateTax = grossProfit * 0.2;
        double netProfit = grossProfit - corporateTax;
        double actualReceived = totalRevenue - totalPaymentGatewayCost;
        
        report.put("period", period);
        report.put("orderCount", orders.size());
        report.put("revenue", Math.round(totalRevenue));
        report.put("vat", Math.round(totalVat));
        report.put("costOfGoods", Math.round(totalCostOfGoods));
        report.put("shippingCost", Math.round(totalShippingCost));
        report.put("paymentGatewayCost", Math.round(totalPaymentGatewayCost));
        report.put("grossProfit", Math.round(grossProfit));
        report.put("corporateTax", Math.round(corporateTax));
        report.put("netProfit", Math.round(netProfit));
        report.put("actualReceived", Math.round(actualReceived));
        
        return report;
    }

    @Override
    public ApiResponse exportReports(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            List<Order> orders = orderRepo.findPaidOrdersBetween(startDateTime, endDateTime);
            List<Map<String, Object>> reports = new ArrayList<>();
            
            for (Order order : orders) {
                Map<String, Object> report = calculateOrderFinancials(order);
                reports.add(report);
            }
            
            byte[] excelData = excelExportService.exportFinancialReport(reports);
            String base64Excel = Base64.getEncoder().encodeToString(excelData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", "BaoCaoTaiChinh_" + startDate + "_" + endDate + ".xlsx");
            result.put("data", base64Excel);
            
            return ApiResponse.success("Xuất báo cáo thành công!", result);
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi xuất báo cáo: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse getAllPeriods() {
        List<AccountingPeriod> periods = periodRepo.findAllByOrderByStartDateDesc();
        return ApiResponse.success("Danh sách kỳ báo cáo", periods);
    }

    @Override
    @Transactional
    public ApiResponse closePeriod(Long id) {
        AccountingPeriod period = periodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kỳ báo cáo"));
        
        if (period.getStatus() == PeriodStatus.CLOSED) {
            return ApiResponse.error("Kỳ này đã được chốt!");
        }
        
        // Kiểm tra sai số
        if (period.getDiscrepancyRate() != null && period.getDiscrepancyRate() > 15) {
            return ApiResponse.error("Sai số vượt quá 15%. Không thể chốt kỳ!");
        }
        
        String currentUser = SecurityUtils.getCurrentUserEmail();
        
        period.setStatus(PeriodStatus.CLOSED);
        period.setClosedBy(currentUser != null ? currentUser : "System");
        period.setClosedAt(LocalDateTime.now());
        
        periodRepo.save(period);
        
        return ApiResponse.success("Đã chốt kỳ báo cáo thành công!", period);
    }

    @Override
    @Transactional
    public ApiResponse reopenPeriod(Long id) {
        AccountingPeriod period = periodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kỳ báo cáo"));
        
        if (period.getStatus() == PeriodStatus.OPEN) {
            return ApiResponse.error("Kỳ này đang mở!");
        }
        
        // Check if user is ADMIN
        if (!SecurityUtils.isAdmin()) {
            return ApiResponse.error("Chỉ Admin mới có quyền mở khóa kỳ báo cáo!");
        }
        
        period.setStatus(PeriodStatus.OPEN);
        period.setClosedBy(null);
        period.setClosedAt(null);
        
        periodRepo.save(period);
        
        return ApiResponse.success("Đã mở khóa kỳ báo cáo!", period);
    }

    private Map<String, Object> calculateSummary(List<PaymentReconciliation> reconciliations) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("total", reconciliations.size());
        summary.put("matched", reconciliations.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.MATCHED).count());
        summary.put("mismatched", reconciliations.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.MISMATCHED).count());
        summary.put("missing", reconciliations.stream()
                .filter(r -> r.getStatus() == ReconciliationStatus.MISSING_IN_SYSTEM || 
                            r.getStatus() == ReconciliationStatus.MISSING_IN_GATEWAY).count());
        
        BigDecimal totalAmount = reconciliations.stream()
                .map(PaymentReconciliation::getSystemAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discrepancyAmount = reconciliations.stream()
                .map(PaymentReconciliation::getDiscrepancy)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.put("totalAmount", totalAmount);
        summary.put("discrepancyAmount", discrepancyAmount);
        
        return summary;
    }
}
