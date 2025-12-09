package com.doan.WEB_TMDT.module.inventory.entity;

public enum ExportStatus {
    CREATED,      // Chờ xử lý
    RECEIVED,     // Đã nhận (đã xuất kho)
    COMPLETED,    // Hoàn thành
    CANCELLED     // Đã hủy
}
