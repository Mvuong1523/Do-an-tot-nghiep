package com.doan.WEB_TMDT.module.order.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.dto.CreateOrderRequest;

public interface OrderService {
    ApiResponse createOrderFromCart(Long userId, CreateOrderRequest request);
    ApiResponse getOrderById(Long orderId, Long userId);
    ApiResponse getOrderByCode(String orderCode, Long userId);
    ApiResponse getMyOrders(Long userId);
    ApiResponse cancelOrder(Long orderId, Long userId, String reason);
}
