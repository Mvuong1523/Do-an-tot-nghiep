package com.doan.WEB_TMDT.module.delivery.controller;

import com.doan.WEB_TMDT.module.delivery.dto.WebhookGhtkPayload;
import com.doan.WEB_TMDT.module.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;

@Tag(name = "Delivery Webhook", description = "Nhận trạng thái từ GHTK")
@RestController
@RequestMapping("/api/delivery/webhook")
@RequiredArgsConstructor
public class DeliveryWebhookController {

    private final DeliveryService deliveryService;

    @Operation(summary = "GHTK webhook")
    @PostMapping("/ghtk")
    public ResponseEntity<?> onGhtkWebhook(@RequestBody WebhookGhtkPayload payload) {
        deliveryService.handleGhtkWebhook(payload);
        return ResponseEntity.ok().body("{\"ok\":true}");
    }
}
