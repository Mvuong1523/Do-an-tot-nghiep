package com.doan.WEB_TMDT.module.accounting.entity;

public enum PayableStatus {
    UNPAID,    // Chưa trả
    PARTIAL,   // Trả một phần
    PAID,      // Đã trả hết
    OVERDUE    // Quá hạn
}
