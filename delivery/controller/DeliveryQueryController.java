package com.doan.WEB_TMDT.module.delivery.controller;

import com.doan.WEB_TMDT.module.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;

@Tag(name = "Delivery Query", description = "Tra cứu thông tin giao vận")
@RestController
@RequestMapping("/api/delivery/track")
@RequiredArgsConstructor
public class DeliveryQueryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "Tra theo orderCode (đơn nội bộ)")
    @GetMapping("/{orderCode}")
    public ResponseEntity<?> byOrderCode(@PathVariable String orderCode) {
        return deliveryService.findByOrderCode(orderCode)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Tra theo provider label (mã vận đơn GHTK)")
    @GetMapping("/label/{label}")
    public ResponseEntity<?> byLabel(@PathVariable String label) {
        return deliveryService.findByProviderLabel(label)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
