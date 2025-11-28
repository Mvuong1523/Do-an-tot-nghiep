package com.doan.WEB_TMDT.module.order.entity;

public enum PaymentStatus {
    UNPAID,         // Chưa thanh toán
    PENDING,        // Đang chờ thanh toán
    PAID,           // Đã thanh toán
    FAILED,         // Thanh toán thất bại
    REFUNDED        // Đã hoàn tiền
}
