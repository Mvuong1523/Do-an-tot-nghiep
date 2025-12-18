package com.doan.WEB_TMDT.module.accounting.entity;

public enum TransactionCategory {
    SALES,          // Doanh thu bán hàng
    SHIPPING,       // Chi phí vận chuyển
    PAYMENT_FEE,    // Phí cổng thanh toán
    TAX,            // Thuế (VAT, TNDN)
    COST_OF_GOODS   // Giá vốn hàng bán (COGS)
}