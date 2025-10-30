package com.doan.WEB_TMDT.module.inventory.entity;

public enum PurchaseStatus {
    CREATED,     // vừa tạo, chưa nhập
    RECEIVING,   // đang nhập hàng (quét serial)
    COMPLETED,   // nhập xong
    CANCELED     // hủy
}
