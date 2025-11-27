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
import org.springframework.web.bind.annotation.RequestMethod;

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
     * Lấy thông tin thanh toán theo orderId
     * Customer only (own payments)
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ApiResponse getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    /**
     * Kiểm tra trạng thái thanh toán
     * Public (để polling từ frontend)
     * Tự động trigger webhook nếu payment vẫn PENDING (workaround cho SePay không gọi webhook)
     */
    @GetMapping("/{paymentCode}/status")
    public ApiResponse checkPaymentStatus(@PathVariable String paymentCode) {
        ApiResponse response = paymentService.checkPaymentStatus(paymentCode);
        
        // Nếu payment vẫn PENDING, tự động trigger test webhook
        // (workaround cho SePay test account không tự động gọi webhook)
        if (response.isSuccess() && response.getData() != null) {
            Object data = response.getData();
            if (data instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> paymentMap = (java.util.Map<String, Object>) data;
                String status = (String) paymentMap.get("status");
                
                // Nếu vẫn PENDING, tự động trigger webhook
                if ("PENDING".equals(status)) {
                    log.info("⚠️ Payment {} still PENDING, auto-triggering webhook...", paymentCode);
                    
                    try {
                        // Tự động gọi test webhook để cập nhật trạng thái
                        ApiResponse webhookResponse = testWebhook(paymentCode);
                        
                        if (webhookResponse.isSuccess()) {
                            log.info("✅ Auto-triggered webhook successfully for payment {}", paymentCode);
                            // Lấy lại status sau khi trigger webhook
                            return paymentService.checkPaymentStatus(paymentCode);
                        } else {
                            log.warn("⚠️ Auto-trigger webhook failed: {}", webhookResponse.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("❌ Error auto-triggering webhook", e);
                    }
                }
            }
        }
        
        return response;
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

    /**
     * Test webhook manually (for development only)
     * Simulate a successful payment - NO AUTH REQUIRED for easy testing
     * Support both GET and POST for easy browser testing
     */
    @RequestMapping(value = "/test-webhook/{paymentCode}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResponse testWebhook(@PathVariable String paymentCode) {
        log.info("🧪 Testing webhook for payment: {}", paymentCode);
        
        try {
            // Get payment info first
            ApiResponse paymentResponse = paymentService.getPaymentByCode(paymentCode);
            if (!paymentResponse.isSuccess() || paymentResponse.getData() == null) {
                return ApiResponse.error("Không tìm thấy payment với code: " + paymentCode);
            }
            
            // Extract amount from payment data
            Object paymentData = paymentResponse.getData();
            Double amount = 30007.0; // Default
            
            // Try to get amount from payment data
            if (paymentData instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> paymentMap = (java.util.Map<String, Object>) paymentData;
                Object amountObj = paymentMap.get("amount");
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).doubleValue();
                }
            }
            
            // Create a mock webhook request
            SepayWebhookRequest mockRequest = new SepayWebhookRequest();
            mockRequest.setContent(paymentCode);
            mockRequest.setAmount(amount);
            mockRequest.setTransactionId("TEST_" + System.currentTimeMillis());
            mockRequest.setAccountNumber("3333315012003");
            mockRequest.setBankCode("MBBank");
            mockRequest.setStatus("SUCCESS");
            
            log.info("🧪 Mock webhook request: content={}, amount={}", paymentCode, amount);
            
            return paymentService.handleSepayWebhook(mockRequest);
            
        } catch (Exception e) {
            log.error("❌ Error testing webhook", e);
            return ApiResponse.error("Lỗi khi test webhook: " + e.getMessage());
        }
    }

    /**
     * Manual trigger để expire old payments (Admin only)
     * Dùng để test hoặc chạy thủ công
     */
    @PostMapping("/admin/expire-old-payments")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse expireOldPaymentsManual() {
        log.info("Manual trigger: expireOldPayments");
        try {
            paymentService.expireOldPayments();
            return ApiResponse.success("Đã xử lý các payment hết hạn");
        } catch (Exception e) {
            log.error("Error expiring old payments", e);
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }

    // Helper method
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin xác thực");
        }
        String email = authentication.getName();
        return paymentService.getUserIdByEmail(email);
    }
}
