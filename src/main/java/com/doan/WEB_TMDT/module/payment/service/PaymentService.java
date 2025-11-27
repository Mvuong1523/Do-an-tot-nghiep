package com.doan.WEB_TMDT.module.payment.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.payment.dto.CreatePaymentRequest;
import com.doan.WEB_TMDT.module.payment.dto.SepayWebhookRequest;

public interface PaymentService {
    Long getUserIdByEmail(String email);
    ApiResponse createPayment(CreatePaymentRequest request, Long userId);
    ApiResponse getPaymentByCode(String paymentCode);
    ApiResponse getPaymentByOrderId(Long orderId);
    ApiResponse getPaymentsByUser(Long userId);
    ApiResponse handleSepayWebhook(SepayWebhookRequest request);
    ApiResponse checkPaymentStatus(String paymentCode);
    void expireOldPayments(); // Cron job để hết hạn các payment cũ
    void deletePaymentByOrderId(Long orderId); // Xóa payment khi hủy order
}
