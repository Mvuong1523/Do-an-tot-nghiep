package com.doan.WEB_TMDT.module.order.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.dto.CreateOrderRequest;
import com.doan.WEB_TMDT.module.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
public class OrderController {

    private final OrderService orderService;

    /**
     * Tạo đơn hàng từ giỏ hàng
     */
    @PostMapping
    public ApiResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return orderService.createOrderFromCart(userId, request);
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    @GetMapping
    public ApiResponse getMyOrders(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return orderService.getMyOrders(userId);
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    @GetMapping("/{orderId}")
    public ApiResponse getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return orderService.getOrderById(orderId, userId);
    }

    /**
     * Lấy chi tiết đơn hàng theo mã
     */
    @GetMapping("/code/{orderCode}")
    public ApiResponse getOrderByCode(
            @PathVariable String orderCode,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return orderService.getOrderByCode(orderCode, userId);
    }

    /**
     * Hủy đơn hàng
     */
    @PutMapping("/{orderId}/cancel")
    public ApiResponse cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return orderService.cancelOrder(orderId, userId, reason);
    }

    // Helper method
    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Extract user ID from authentication
        // For now, return mock ID
        return 1L;
    }
}
