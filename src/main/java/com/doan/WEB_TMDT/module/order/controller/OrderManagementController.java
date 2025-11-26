package com.doan.WEB_TMDT.module.order.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SALES_STAFF')")
public class OrderManagementController {

    private final OrderService orderService;

    /**
     * Lấy tất cả đơn hàng với phân trang và filter
     * GET /api/admin/orders?status=PENDING&page=0&size=20&search=keyword
     */
    @GetMapping
    public ApiResponse getAllOrders(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return orderService.getAllOrders(status, page, size);
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     * GET /api/admin/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ApiResponse getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Thống kê đơn hàng theo trạng thái
     * GET /api/admin/orders/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse getOrderStatistics() {
        return orderService.getOrderStatistics();
    }

    /**
     * Xác nhận đơn hàng (PENDING -> CONFIRMED)
     * PUT /api/admin/orders/{orderId}/confirm
     */
    @PutMapping("/{orderId}/confirm")
    public ApiResponse confirmOrder(@PathVariable Long orderId) {
        return orderService.confirmOrder(orderId);
    }

    /**
     * Đánh dấu đang giao hàng (CONFIRMED -> SHIPPING)
     * PUT /api/admin/orders/{orderId}/shipping
     */
    @PutMapping("/{orderId}/shipping")
    public ApiResponse markAsShipping(@PathVariable Long orderId) {
        return orderService.markAsShipping(orderId);
    }

    /**
     * Đánh dấu đã giao hàng (SHIPPING -> DELIVERED)
     * PUT /api/admin/orders/{orderId}/delivered
     */
    @PutMapping("/{orderId}/delivered")
    public ApiResponse markAsDelivered(@PathVariable Long orderId) {
        return orderService.markAsDelivered(orderId);
    }

    /**
     * Hủy đơn hàng (PENDING/CONFIRMED -> CANCELLED)
     * PUT /api/admin/orders/{orderId}/cancel
     */
    @PutMapping("/{orderId}/cancel")
    public ApiResponse cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        return orderService.cancelOrder(orderId, reason);
    }

    /**
     * Cập nhật trạng thái đơn hàng (flexible)
     * PUT /api/admin/orders/{orderId}/status?status=CONFIRMED
     */
    @PutMapping("/{orderId}/status")
    public ApiResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }
}
