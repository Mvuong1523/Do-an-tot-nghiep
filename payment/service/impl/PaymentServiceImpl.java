package com.doan.WEB_TMDT.module.payment.service.impl;

import com.doan.WEB_TMDT.module.payment.entity.PaymentTransaction;
import com.doan.WEB_TMDT.module.payment.repository.PaymentTransactionRepository;
import com.doan.WEB_TMDT.module.payment.service.PaymentService;
import com.doan.WEB_TMDT.module.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void handleWebhook(Map<String, Object> payload) {
        System.out.println(" Nhận IPN từ SePay: " + payload);

        // ---  Lấy các object con ---
        Map<String, Object> order = (Map<String, Object>) payload.get("order");
        Map<String, Object> transaction = (Map<String, Object>) payload.get("transaction");

        if (order == null || transaction == null) {
            System.err.println("⚠ Webhook thiếu order hoặc transaction!");
            return;
        }

        // ---  Lấy dữ liệu thật từ JSON ---
        String paymentCode = (String) order.get("order_id"); // mã đơn hàng từ SePay
        String statusStr = (String) order.get("order_status"); // CAPTURED, FAILED, ...
        String bankRef = (String) transaction.get("transaction_id"); // mã giao dịch
        BigDecimal amount = new BigDecimal(order.get("order_amount").toString());

        // ---  Mapping sang enum ---
        PaymentStatus status;
        switch (statusStr.toUpperCase()) {
            case "CAPTURED" -> status = PaymentStatus.PAID;
            case "FAILED" -> status = PaymentStatus.FAILED;
            default -> status = PaymentStatus.PENDING;
        }

        // ---  Tìm hoặc tạo PaymentTransaction ---
        PaymentTransaction transactionEntity = paymentRepository.findByPaymentCode(paymentCode)
                .orElseGet(() -> PaymentTransaction.builder()
                        .paymentCode(paymentCode)
                        .amount(amount)
                        .createdAt(LocalDateTime.now())
                        .build());

        // ---  Cập nhật thông tin mới ---
        transactionEntity.setStatus(status);
        transactionEntity.setBankReference(bankRef);
        transactionEntity.setPaidAt(LocalDateTime.now());
        transactionEntity.setRawPayload(payload.toString());

        paymentRepository.save(transactionEntity);

        System.out.printf("IPN OK | Mã: %s | Trạng thái: %s | Số tiền: %s%n",
                paymentCode, status, amount);
    }

    @Override
    public String generatePaymentQRCode(Long orderId, String paymentCode, String bankName, String description, BigDecimal amount) {
        // 🧾 Giả lập tạo QR code thật (có thể gọi SePay API ở đây)
        String qrBase64 = "data:image/png;base64," + UUID.randomUUID(); // Mock QR

        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(orderId)
                .paymentCode(paymentCode)
                .bankReference(bankName)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .qrBase64(qrBase64)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(tx);

        return qrBase64;
    }
}
