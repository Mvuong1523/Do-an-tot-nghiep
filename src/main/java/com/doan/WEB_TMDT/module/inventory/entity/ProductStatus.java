package com.doan.WEB_TMDT.module.inventory.entity;

public enum ProductStatus {
    IN_STOCK,       // đang trong kho
    RESERVED,       // đã giữ cho đơn hàng
    SOLD,           // đã bán
    RETURNED,       // đã trả lại kho
    WARRANTY,       // đang gửi bảo hành
    DAMAGED        // hư, không dùng được
}
