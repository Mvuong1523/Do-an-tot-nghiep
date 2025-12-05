package com.doan.WEB_TMDT.module.webhook.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.webhook.dto.GHNWebhookRequest;
import com.doan.WEB_TMDT.module.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * GHN Webhook endpoint
     * GHN sẽ gọi endpoint này khi có thay đổi trạng thái đơn hàng
     * 
     * POST /api/webhooks/ghn
     * Content-Type: application/json
     * 
     * Body: {
     *   "orderCode": "GHNABCD1234",
     *   "status": "delivered",
     *   "statusText": "Đã giao hàng",
     *   "updatedDate": 1701849600,
     *   "currentWarehouse": "Kho Hà Nội",
     *   "description": "Giao hàng thành công",
     *   "codAmount": 500000,
     *   "shippingFee": 30000,
     *   "partnerCode": "ORD20231205001"
     * }
     */
    @PostMapping("/ghn")
    public ApiResponse handleGHNWebhook(@RequestBody GHNWebhookRequest request) {
        log.info("=== GHN Webhook Received ===");
        log.info("Order Code: {}", request.getOrderCode());
        log.info("Status: {} ({})", request.getStatus(), request.getStatusText());
        log.info("Partner Code: {}", request.getPartnerCode());
        log.info("Description: {}", request.getDescription());
        
        try {
            webhookService.handleGHNWebhook(request);
            return ApiResponse.success("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing GHN webhook: {}", e.getMessage(), e);
            // Return success anyway to avoid GHN retrying
            return ApiResponse.success("Webhook received");
        }
    }
}
