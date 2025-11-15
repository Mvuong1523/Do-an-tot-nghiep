package com.doan.WEB_TMDT.module.delivery.entity;

public enum DeliveryStatus {
    CREATED,          // tạo record local
    SENT_TO_CARRIER,  // đã gọi sang hãng
    ACCEPTED,         // (để dành, map webhook)
    REJECTED,         // hãng báo lỗi
    DELIVERING,
    DELIVERED,
    RETURNED
}
