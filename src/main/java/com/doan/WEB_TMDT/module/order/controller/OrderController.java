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
        Long customerId = getCustomerIdFromAuth(authentication);
        return orderService.createOrderFromCart(customerId, request);
    }

    /**
     * Lấy danh sách đơn hàng của customer
     */
    @GetMapping
    public ApiResponse getMyOrders(Authentication authentication) {
        Long customerId = getCustomerIdFromAuth(authentication);
        return orderService.getMyOrders(customerId);
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    @GetMapping("/{orderId}")
    public ApiResponse getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long customerId = getCustomerIdFromAuth(authentication);
        return orderService.getOrderById(orderId, customerId);
    }

    /**
     * Lấy chi tiết đơn hàng theo mã
     */
    @GetMapping("/code/{orderCode}")
    public ApiResponse getOrderByCode(
            @PathVariable String orderCode,
            Authentication authentication) {
        Long customerId = getCustomerIdFromAuth(authentication);
        return orderService.getOrderByCode(orderCode, customerId);
    }

    /**
     * Hủy đơn hàng (Customer)
     */
    @PutMapping("/{orderId}/cancel")
    public ApiResponse cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        Long customerId = getCustomerIdFromAuth(authentication);
        return orderService.cancelOrderByCustomer(orderId, customerId, reason);
    }

    // Helper method
    private Long getCustomerIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin xác thực");
        }
        String email = authentication.getName();
        return orderService.getCustomerIdByEmail(email);
    }
}
