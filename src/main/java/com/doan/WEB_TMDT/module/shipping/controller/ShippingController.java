package com.doan.WEB_TMDT.module.shipping.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.shipping.dto.CalculateShippingFeeRequest;
import com.doan.WEB_TMDT.module.shipping.dto.ShippingFeeResponse;
import com.doan.WEB_TMDT.module.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/calculate-fee")
    public ResponseEntity<?> calculateShippingFee(
            @RequestBody CalculateShippingFeeRequest request) {
        try {
            log.info("Calculating shipping fee for: {}", request);
            ShippingFeeResponse response = shippingService.calculateShippingFee(request);
            return ResponseEntity.ok(ApiResponse.success("Tính phí thành công", response));
        } catch (Exception e) {
            log.error("Error calculating shipping fee", e);
            return ResponseEntity.ok(ApiResponse.error("Không thể tính phí vận chuyển: " + e.getMessage()));
        }
    }
}
