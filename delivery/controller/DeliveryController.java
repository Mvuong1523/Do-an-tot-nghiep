package com.doan.WEB_TMDT.module.delivery.controller;

import com.doan.WEB_TMDT.module.delivery.dto.*;
import com.doan.WEB_TMDT.module.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;

@Tag(name = "Delivery", description = "Tính phí & tạo đơn giao hàng (sandbox)")
@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "Ping carrier (kiểm tra token/kết nối)")
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(deliveryService.ping());
    }

    @Operation(summary = "Tính phí vận chuyển")
    @PostMapping("/fee")
    public ResponseEntity<?> calcFee(@RequestBody DeliveryCalculateFeeRequest req) {
        return ResponseEntity.ok(deliveryService.calculateFee(req));
    }

    @Operation(summary = "Tạo đơn giao hàng từ order của shop")
    @PostMapping("/orders")
    public ResponseEntity<?> createOrderFromShop(@RequestBody ShopOrderRequest shopOrder) {
        return ResponseEntity.ok(deliveryService.createOrderFromShop(shopOrder));
    }
}
