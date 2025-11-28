package com.doan.WEB_TMDT.module.payment.entity;

public enum PaymentStatus {
    PENDING,    // Đang chờ thanh toán
    PROCESSING, // Đang xử lý
    SUCCESS,    // Thanh toán thành công
    FAILED,     // Thanh toán thất bại
    EXPIRED,    // Hết hạn
    CANCELLED   // Đã hủy
}
