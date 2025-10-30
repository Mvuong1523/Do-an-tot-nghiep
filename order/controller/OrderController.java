package com.doan.WEB_TMDT.module.order.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.dto.CreateOrderRequest;
import com.doan.WEB_TMDT.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ApiResponse createOrder(@RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    @GetMapping("/customer/{id}")
    public ApiResponse getOrdersByCustomer(@PathVariable Long id) {
        return orderService.getOrdersByCustomer(id);
    }
}
