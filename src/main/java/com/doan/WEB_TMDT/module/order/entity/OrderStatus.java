package com.doan.WEB_TMDT.module.order.entity;

public enum OrderStatus {
    PENDING,        // Chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    PROCESSING,     // Đang xử lý
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao hàng
    COMPLETED,      // Hoàn thành
    CANCELLED,      // Đã hủy
    RETURNED        // Đã trả hàng
}
