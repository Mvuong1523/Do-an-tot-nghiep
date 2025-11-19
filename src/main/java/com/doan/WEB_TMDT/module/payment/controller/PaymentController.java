package com.doan.WEB_TMDT.module.payment.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.payment.dto.CreatePaymentRequest;
import com.doan.WEB_TMDT.module.payment.dto.SepayWebhookRequest;
import com.doan.WEB_TMDT.module.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo thanh toán mới
     * Customer only
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ApiResponse createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        return paymentService.createPayment(request, userId);
    }

    /**
     * Lấy thông tin thanh toán theo mã
     * Customer only (own payments)
     */
    @GetMapping("/{paymentCode}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ApiResponse getPaymentByCode(@PathVariable String paymentCode) {
        return paymentService.getPaymentByCode(paymentCode);
    }

    /**
     * Kiểm tra trạng thái thanh toán
     * Public (để polling từ frontend)
     */
    @GetMapping("/{paymentCode}/status")
    public ApiResponse checkPaymentStatus(@PathVariable String paymentCode) {
        return paymentService.checkPaymentStatus(paymentCode);
    }

    /**
     * Lấy danh sách thanh toán của user
     * Customer only
     */
    @GetMapping("/my-payments")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ApiResponse getMyPayments(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return paymentService.getPaymentsByUser(userId);
    }

    /**
     * Webhook từ SePay
     * Public (SePay gọi vào)
     */
    @PostMapping("/sepay/webhook")
    public ApiResponse handleSepayWebhook(@RequestBody SepayWebhookRequest request) {
        log.info("Received SePay webhook for payment: {}", request.getContent());
        return paymentService.handleSepayWebhook(request);
    }

    // Helper method
    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Extract user ID from authentication
        // For now, return mock ID
        return 1L;
    }
}
