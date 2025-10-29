package com.doan.WEB_TMDT.module.order.entity;

public enum OrderStatus {
    PENDING,       // Đã tạo đơn, chưa thanh toán
    PAID,          // Đã thanh toán
    SHIPPING,      // Đang giao hàng
    COMPLETED,     // Hoàn tất
    CANCELED       // Hủy
}
