package com.doan.WEB_TMDT.module.inventory.entity;

public enum POStatus {
    CREATED,     // vừa tạo, chưa nhập
    RECEIVING,   // đang nhập hàng (quét serial)
    PENDING,
    COMPLETED,   // nhập xong
    CANCELED     // hủy
}
