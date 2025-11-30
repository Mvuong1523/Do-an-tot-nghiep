package com.doan.WEB_TMDT.module.inventory.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE')")
public class InventoryOrderController {

    private final OrderService orderService;

    /**
     * Lấy danh sách đơn hàng cần xuất kho (status = CONFIRMED)
     * Quản lý kho xem để chuẩn bị hàng
     */
    @GetMapping("/pending-export")
    public ApiResponse getOrdersPendingExport(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        // Lấy các đơn hàng đã CONFIRMED, chưa xuất kho
        return orderService.getAllOrders("CONFIRMED", page, size);
    }

    /**
     * Lấy chi tiết đơn hàng để chuẩn bị xuất kho
     */
    @GetMapping("/{orderId}")
    public ApiResponse getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Lấy thống kê đơn hàng cần xuất
     */
    @GetMapping("/statistics")
    public ApiResponse getExportStatistics() {
        return orderService.getOrderStatistics();
    }
}
