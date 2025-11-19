package com.doan.WEB_TMDT.module.payment.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.payment.dto.CreatePaymentRequest;
import com.doan.WEB_TMDT.module.payment.dto.SepayWebhookRequest;

public interface PaymentService {
    ApiResponse createPayment(CreatePaymentRequest request, Long userId);
    ApiResponse getPaymentByCode(String paymentCode);
    ApiResponse getPaymentsByUser(Long userId);
    ApiResponse handleSepayWebhook(SepayWebhookRequest request);
    ApiResponse checkPaymentStatus(String paymentCode);
    void expireOldPayments(); // Cron job để hết hạn các payment cũ
}
