package com.doan.WEB_TMDT.service.impl;

import com.doan.WEB_TMDT.dto.DashboardStatsDTO;
import com.doan.WEB_TMDT.dto.OrderDTO;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.entity.OrderStatus;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.repository.UserRepository;
import com.doan.WEB_TMDT.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Get current stats
        Long totalOrders = orderRepository.count();
        
        // Calculate revenue and profit from delivered orders
        List<Order> deliveredOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());
        
        Double totalRevenue = deliveredOrders.stream()
                .mapToDouble(Order::getTotal)
                .sum();
        
        // Calculate profit (revenue - cost)
        Double totalCost = deliveredOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(item -> {
                    // Get warehouse product cost
                    if (item.getWarehouseProduct() != null && item.getWarehouseProduct().getImportPrice() != null) {
                        return item.getWarehouseProduct().getImportPrice() * item.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
        
        Double totalProfit = totalRevenue - totalCost;
        Double profitMargin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100 : 0.0;
        
        Long totalProducts = productRepository.count();
        Long totalCustomers = userRepository.countByRole("CUSTOMER");
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long lowStockProducts = productRepository.countByStockQuantityLessThan(10);

        // Calculate percentage changes (comparing with last month)
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        Long lastMonthOrders = orderRepository.countByCreatedAtAfter(lastMonth);
        Double ordersChangePercent = calculatePercentageChange(totalOrders, lastMonthOrders);

        List<Order> lastMonthDeliveredOrders = orderRepository.findByCreatedAtAfter(lastMonth).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());
        
        Double lastMonthRevenue = lastMonthDeliveredOrders.stream()
                .mapToDouble(Order::getTotal)
                .sum();
        Double revenueChangePercent = calculatePercentageChange(totalRevenue, lastMonthRevenue);
        
        Double lastMonthCost = lastMonthDeliveredOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToDouble(item -> {
                    if (item.getWarehouseProduct() != null && item.getWarehouseProduct().getImportPrice() != null) {
                        return item.getWarehouseProduct().getImportPrice() * item.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
        
        Double lastMonthProfit = lastMonthRevenue - lastMonthCost;
        Double profitChangePercent = calculatePercentageChange(totalProfit, lastMonthProfit);

        return DashboardStatsDTO.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalProfit(totalProfit)
                .profitMargin(Math.round(profitMargin * 10.0) / 10.0)
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .ordersChangePercent(ordersChangePercent)
                .revenueChangePercent(revenueChangePercent)
                .profitChangePercent(profitChangePercent)
                .productsChangePercent(0.0) // Can be calculated if needed
                .customersChangePercent(0.0) // Can be calculated if needed
                .build();
    }

    @Override
    public List<OrderDTO> getRecentOrders(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findAll(pageRequest).getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotal())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "N/A")
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : "N/A")
                .build();
    }

    private Double calculatePercentageChange(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return 0.0;
        }
        double change = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        return Math.round(change * 10.0) / 10.0; // Round to 1 decimal place
    }
}
