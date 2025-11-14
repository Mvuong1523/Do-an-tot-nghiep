package com.doan.WEB_TMDT.module.cart.entity;

public enum CartStatus {
    ACTIVE,       // giỏ đang dùng
    CHECKED_OUT,  // đã được dùng để tạo đơn hàng
    ABANDONED     // bỏ dở / hết hạn (tùy bạn dùng hay không)
}
