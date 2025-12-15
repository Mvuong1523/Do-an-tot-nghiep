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
import java.math.RoundingMode;
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

        // Tự động đối soát với dữ liệu hệ thống
        for (PaymentReconciliation reconciliation : reconciliations) {
            Optional<Order> orderOpt = orderRepo.findByOrderCode(reconciliation.getOrderId());
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                BigDecimal systemAmount = BigDecimal.valueOf(order.getTotal());
                
                // Cập nhật số tiền hệ thống nếu khác
                if (!systemAmount.equals(reconciliation.getSystemAmount())) {
                    reconciliation.setSystemAmount(systemAmount);
                    reconciliation.setDiscrepancy(systemAmount.subtract(reconciliation.getGatewayAmount()).abs());
                    
                    // Cập nhật trạng thái
                    if (reconciliation.getDiscrepancy().compareTo(BigDecimal.ZERO) == 0) {
                        reconciliation.setStatus(ReconciliationStatus.MATCHED);
                    } else {
                        reconciliation.setStatus(ReconciliationStatus.MISMATCHED);
                    }
                }
            }
        }

        // Lưu các thay đổi
        reconciliationRepo.saveAll(reconciliations);

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
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // Lấy tất cả đơn hàng trong khoảng thời gian
        List<Order> orders = orderRepo.findPaidOrdersBetween(startDateTime, endDateTime);
        
        BigDecimal totalShippingFeeCollected = BigDecimal.ZERO; // Phí vận chuyển thu từ khách
        BigDecimal totalShippingCostPaid = BigDecimal.ZERO;     // Chi phí trả cho đối tác vận chuyển
        int totalOrders = orders.size();
        
        List<Map<String, Object>> shippingDetails = new ArrayList<>();
        
        for (Order order : orders) {
            BigDecimal shippingFeeCollected = BigDecimal.valueOf(order.getShippingFee());
            // Giả định chi phí thực tế trả cho đối tác = 80% phí thu từ khách
            BigDecimal actualShippingCost = shippingFeeCollected.multiply(BigDecimal.valueOf(0.8));
            
            totalShippingFeeCollected = totalShippingFeeCollected.add(shippingFeeCollected);
            totalShippingCostPaid = totalShippingCostPaid.add(actualShippingCost);
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("orderId", order.getOrderCode());
            detail.put("shippingFeeCollected", shippingFeeCollected);
            detail.put("actualShippingCost", actualShippingCost);
            detail.put("profit", shippingFeeCollected.subtract(actualShippingCost));
            detail.put("orderDate", order.getCreatedAt().toLocalDate());
            detail.put("shippingAddress", order.getShippingAddress());
            
            shippingDetails.add(detail);
        }
        
        BigDecimal shippingProfit = totalShippingFeeCollected.subtract(totalShippingCostPaid);
        BigDecimal profitMargin = totalShippingFeeCollected.compareTo(BigDecimal.ZERO) > 0 
            ? shippingProfit.divide(totalShippingFeeCollected, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        Map<String, Object> result = new HashMap<>();
        result.put("period", startDate + " - " + endDate);
        result.put("totalOrders", totalOrders);
        result.put("totalShippingFeeCollected", totalShippingFeeCollected);
        result.put("totalShippingCostPaid", totalShippingCostPaid);
        result.put("shippingProfit", shippingProfit);
        result.put("profitMargin", profitMargin);
        result.put("details", shippingDetails);
        
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
        double shippingCost = order.getShippingFee();
        double paymentGatewayCost = revenue * 0.02; // Phí cổng thanh toán 2%
        
        double totalCosts = shippingCost + paymentGatewayCost;
        double grossProfit = revenue - totalCosts;
        
        double vat = grossProfit * 0.1; // VAT 10%
        double profitAfterVAT = grossProfit - vat;
        double corporateTax = profitAfterVAT * 0.2; // Thuế TNDN 20%
        double netProfit = profitAfterVAT - corporateTax;
        
        report.put("orderId", order.getOrderCode());
        report.put("date", order.getCreatedAt().toLocalDate().toString());
        report.put("revenue", Math.round(revenue));
        report.put("shippingCost", Math.round(shippingCost));
        report.put("paymentGatewayCost", Math.round(paymentGatewayCost));
        report.put("totalCosts", Math.round(totalCosts));
        report.put("grossProfit", Math.round(grossProfit));
        report.put("vat", Math.round(vat));
        report.put("corporateTax", Math.round(corporateTax));
        report.put("netProfit", Math.round(netProfit));
        
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
        double totalShippingCost = 0;
        double totalPaymentGatewayCost = 0;
        
        for (Order order : orders) {
            double revenue = order.getTotal();
            totalRevenue += revenue;
            totalShippingCost += order.getShippingFee();
            totalPaymentGatewayCost += revenue * 0.02;
        }
        
        double totalCosts = totalShippingCost + totalPaymentGatewayCost;
        double grossProfit = totalRevenue - totalCosts;
        
        double totalVat = grossProfit * 0.1;
        double profitAfterVAT = grossProfit - totalVat;
        double corporateTax = profitAfterVAT * 0.2;
        double netProfit = profitAfterVAT - corporateTax;
        
        report.put("period", period);
        report.put("orderCount", orders.size());
        report.put("revenue", Math.round(totalRevenue));
        report.put("shippingCost", Math.round(totalShippingCost));
        report.put("paymentGatewayCost", Math.round(totalPaymentGatewayCost));
        report.put("totalCosts", Math.round(totalCosts));
        report.put("grossProfit", Math.round(grossProfit));
        report.put("vat", Math.round(totalVat));
        report.put("corporateTax", Math.round(corporateTax));
        report.put("netProfit", Math.round(netProfit));
        
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
        
        // Tính toán lại sai số trước khi chốt
        LocalDateTime startDateTime = period.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = period.getEndDate().atTime(23, 59, 59);
        
        // Tính doanh thu thực tế từ đơn hàng
        Double actualRevenue = orderRepo.sumTotalByDateRange(startDateTime, endDateTime);
        if (actualRevenue == null) actualRevenue = 0.0;
        
        // Tính tổng sai lệch từ đối soát thanh toán
        Double totalDiscrepancy = reconciliationRepo.sumDiscrepancyByDateRange(startDateTime, endDateTime);
        if (totalDiscrepancy == null) totalDiscrepancy = 0.0;
        
        // Cập nhật thông tin kỳ
        period.setTotalRevenue(BigDecimal.valueOf(actualRevenue));
        period.setDiscrepancyAmount(BigDecimal.valueOf(totalDiscrepancy));
        
        // Tính tỷ lệ sai số
        double discrepancyRate = actualRevenue > 0 ? (totalDiscrepancy / actualRevenue) * 100 : 0;
        period.setDiscrepancyRate(discrepancyRate);
        
        // Kiểm tra sai số > 15%
        if (discrepancyRate > 15) {
            periodRepo.save(period); // Lưu thông tin đã cập nhật
            return ApiResponse.error(String.format(
                "Sai số %.2f%% vượt quá 15%%. Vui lòng kiểm tra và xử lý các sai lệch trước khi chốt kỳ. " +
                "Tổng sai lệch: %,.0f ₫ / Tổng doanh thu: %,.0f ₫", 
                discrepancyRate, totalDiscrepancy, actualRevenue));
        }
        
        // Đếm số đơn hàng trong kỳ
        Long orderCount = orderRepo.countPaidOrdersBetween(startDateTime, endDateTime);
        period.setTotalOrders(orderCount != null ? orderCount.intValue() : 0);
        
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

    @Override
    public ApiResponse exportShippingReconciliation(LocalDate startDate, LocalDate endDate) {
        try {
            // Lấy dữ liệu đối soát vận chuyển
            ApiResponse reconciliationResponse = getShippingReconciliation(startDate, endDate);
            if (!reconciliationResponse.isSuccess()) {
                return reconciliationResponse;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) reconciliationResponse.getData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");
            
            byte[] excelData = excelExportService.exportShippingReconciliation(details);
            String base64Excel = Base64.getEncoder().encodeToString(excelData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", "DoiSoatVanChuyen_" + startDate + "_" + endDate + ".xlsx");
            result.put("data", base64Excel);
            
            return ApiResponse.success("Xuất báo cáo đối soát vận chuyển thành công!", result);
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi xuất báo cáo: " + e.getMessage());
        }
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
