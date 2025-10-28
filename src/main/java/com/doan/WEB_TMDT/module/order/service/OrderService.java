package com.doan.WEB_TMDT.module.order.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.dto.CreateOrderRequest;

public interface OrderService {
    ApiResponse createOrder(CreateOrderRequest req);
    ApiResponse getOrdersByCustomer(Long customerId);
}
