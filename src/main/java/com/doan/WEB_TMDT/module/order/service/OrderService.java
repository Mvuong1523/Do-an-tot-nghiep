package com.doan.WEB_TMDT.module.order.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.dto.CreateOrderRequest;

public interface OrderService {
    // Helper method
    Long getUserIdByEmail(String email);
    
    // Customer endpoints
    ApiResponse createOrderFromCart(Long userId, CreateOrderRequest request);
    ApiResponse getOrderById(Long orderId, Long userId);
    ApiResponse getOrderByCode(String orderCode, Long userId);
    ApiResponse getMyOrders(Long userId);
    ApiResponse cancelOrderByCustomer(Long orderId, Long userId, String reason);
    
    // Admin/Staff endpoints
    ApiResponse getAllOrders(String status, int page, int size);
    ApiResponse getOrderById(Long orderId);
    ApiResponse getOrderStatistics();
    ApiResponse confirmOrder(Long orderId);
    ApiResponse updateOrderStatus(Long orderId, String status);
    ApiResponse markAsShipping(Long orderId);
    ApiResponse markAsDelivered(Long orderId);
    ApiResponse cancelOrder(Long orderId, String reason);
}
