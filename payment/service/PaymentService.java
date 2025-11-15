package com.doan.WEB_TMDT.module.payment.service;

import java.util.Map;

public interface PaymentService {
    // Xử lý webhook IPN từ SePay
    void handleWebhook(Map<String, Object> payload);

    // Tạo QR thanh toán cho đơn hàng
    String generatePaymentQRCode(Long orderId, String paymentCode, String bankName, String description, java.math.BigDecimal amount);
}
