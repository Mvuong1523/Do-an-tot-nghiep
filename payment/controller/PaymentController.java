package com.doan.WEB_TMDT.module.payment.controller;

import com.doan.WEB_TMDT.module.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ✅ Nhận webhook từ SePay
    @PostMapping("/ipn")
    public ResponseEntity<String> receiveIPN(@RequestBody Map<String, Object> payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok("OK");
    }

    // ✅ Tạo mã QR thanh toán (demo)
    @PostMapping("/create-qrcode")
    public ResponseEntity<Map<String, Object>> createQr(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String paymentCode = (String) body.getOrDefault("paymentCode", "PAY-" + orderId);
        String bankName = (String) body.getOrDefault("bankName", "Techcombank");
        String description = (String) body.getOrDefault("description", "Thanh toán đơn hàng #" + orderId);

        String qr = paymentService.generatePaymentQRCode(orderId, paymentCode, bankName, description, amount);
        return ResponseEntity.ok(Map.of("paymentCode", paymentCode, "qrBase64", qr));
    }
}
