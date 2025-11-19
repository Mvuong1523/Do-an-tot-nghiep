package com.doan.WEB_TMDT.module.payment.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import com.doan.WEB_TMDT.module.payment.dto.CreatePaymentRequest;
import com.doan.WEB_TMDT.module.payment.dto.PaymentResponse;
import com.doan.WEB_TMDT.module.payment.dto.SepayWebhookRequest;
import com.doan.WEB_TMDT.module.payment.entity.Payment;
import com.doan.WEB_TMDT.module.payment.entity.PaymentMethod;
import com.doan.WEB_TMDT.module.payment.entity.PaymentStatus;
import com.doan.WEB_TMDT.module.payment.repository.PaymentRepository;
import com.doan.WEB_TMDT.module.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${sepay.bank.code:VCB}")
    private String sepayBankCode;

    @Value("${sepay.bank.account.number:1234567890}")
    private String sepayAccountNumber;

    @Value("${sepay.bank.account.name:CONG TY TNHH TECHMART}")
    private String sepayAccountName;

    @Value("${sepay.api.secret:demo_secret}")
    private String sepaySecretKey;

    @Override
    @Transactional
    public ApiResponse createPayment(CreatePaymentRequest request, Long userId) {
        // 1. Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("Bạn không có quyền thanh toán đơn hàng này");
        }

        // 2. Check if payment already exists
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            return ApiResponse.error("Đơn hàng này đã có thanh toán");
        }

        // 3. Validate amount
        if (!request.getAmount().equals(order.getTotal())) {
            return ApiResponse.error("Số tiền thanh toán không khớp với đơn hàng");
        }

        // 4. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 5. Generate payment code
        String paymentCode = generatePaymentCode();

        // 6. Generate QR Code URL (SePay format)
        String qrCodeUrl = generateSepayQrCode(paymentCode, request.getAmount());

        // 7. Create payment
        Payment payment = Payment.builder()
                .paymentCode(paymentCode)
                .order(order)
                .user(user)
                .amount(request.getAmount())
                .method(PaymentMethod.SEPAY)
                .status(PaymentStatus.PENDING)
                .sepayBankCode(sepayBankCode)
                .sepayAccountNumber(sepayAccountNumber)
                .sepayAccountName(sepayAccountName)
                .sepayContent(paymentCode)
                .sepayQrCode(qrCodeUrl)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 8. Update order
        order.setPaymentId(savedPayment.getId());
        order.setPaymentStatus(com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PENDING);
        orderRepository.save(order);

        // 9. Build response
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .paymentCode(savedPayment.getPaymentCode())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus().name())
                .bankCode(savedPayment.getSepayBankCode())
                .accountNumber(savedPayment.getSepayAccountNumber())
                .accountName(savedPayment.getSepayAccountName())
                .content(savedPayment.getSepayContent())
                .qrCodeUrl(savedPayment.getSepayQrCode())
                .expiredAt(savedPayment.getExpiredAt().toString())
                .message("Vui lòng quét mã QR hoặc chuyển khoản với nội dung: " + paymentCode)
                .build();

        return ApiResponse.success("Tạo thanh toán thành công", response);
    }

    @Override
    public ApiResponse getPaymentByCode(String paymentCode) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        PaymentResponse response = toPaymentResponse(payment);
        return ApiResponse.success("Thông tin thanh toán", response);
    }

    @Override
    public ApiResponse getPaymentsByUser(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Danh sách thanh toán", responses);
    }

    @Override
    @Transactional
    public ApiResponse handleSepayWebhook(SepayWebhookRequest request) {
        log.info("Received SePay webhook: {}", request);

        try {
            // 1. Verify signature
            if (!verifySignature(request)) {
                log.error("Invalid signature from SePay webhook");
                return ApiResponse.error("Chữ ký không hợp lệ");
            }

            // 2. Find payment by content (paymentCode)
            Payment payment = paymentRepository.findByPaymentCode(request.getContent())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với mã: " + request.getContent()));

            // 3. Check if already processed
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("Payment already processed: {}", payment.getPaymentCode());
                return ApiResponse.success("Thanh toán đã được xử lý");
            }

            // 4. Check amount
            if (!payment.getAmount().equals(request.getAmount())) {
                log.error("Amount mismatch. Expected: {}, Received: {}", payment.getAmount(), request.getAmount());
                return ApiResponse.error("Số tiền không khớp");
            }

            // 5. Check if expired
            if (LocalDateTime.now().isAfter(payment.getExpiredAt())) {
                payment.setStatus(PaymentStatus.EXPIRED);
                payment.setFailureReason("Thanh toán đã hết hạn");
                paymentRepository.save(payment);
                return ApiResponse.error("Thanh toán đã hết hạn");
            }

            // 6. Update payment
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setSepayTransactionId(request.getTransactionId());
            payment.setPaidAt(LocalDateTime.now());
            payment.setSepayResponse(request.toString());
            paymentRepository.save(payment);

            // 7. Update order
            Order order = payment.getOrder();
            order.setPaymentStatus(com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            order.setStatus(com.doan.WEB_TMDT.module.order.entity.OrderStatus.CONFIRMED);
            order.setConfirmedAt(LocalDateTime.now());
            orderRepository.save(order);

            log.info("Payment processed successfully: {}", payment.getPaymentCode());

            // TODO: Send email confirmation
            // TODO: Send notification

            return ApiResponse.success("Xử lý thanh toán thành công");

        } catch (Exception e) {
            log.error("Error processing SePay webhook", e);
            return ApiResponse.error("Lỗi xử lý webhook: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse checkPaymentStatus(String paymentCode) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        // Check if expired
        if (payment.getStatus() == PaymentStatus.PENDING && 
            LocalDateTime.now().isAfter(payment.getExpiredAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
        }

        PaymentResponse response = toPaymentResponse(payment);
        return ApiResponse.success("Trạng thái thanh toán", response);
    }

    @Override
    @Transactional
    public void expireOldPayments() {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> expiredPayments = paymentRepository
                .findByStatusAndExpiredAtBefore(PaymentStatus.PENDING, now);

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment.setFailureReason("Hết hạn thanh toán");
            paymentRepository.save(payment);

            // Update order
            Order order = payment.getOrder();
            if (order.getStatus() == com.doan.WEB_TMDT.module.order.entity.OrderStatus.PENDING) {
                order.setStatus(com.doan.WEB_TMDT.module.order.entity.OrderStatus.CANCELLED);
                order.setCancelledAt(now);
                order.setCancelReason("Hết hạn thanh toán");
                orderRepository.save(order);
            }
        }

        log.info("Expired {} old payments", expiredPayments.size());
    }

    // Helper methods

    private String generatePaymentCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = new Random().nextInt(9999);
        String code = "PAY" + date + String.format("%04d", random);

        // Check if exists
        if (paymentRepository.existsByPaymentCode(code)) {
            return generatePaymentCode(); // Retry
        }

        return code;
    }

    private String generateSepayQrCode(String content, Double amount) {
        // SePay QR Code format
        // https://img.vietqr.io/image/[BANK]-[ACCOUNT]-[TEMPLATE].png?amount=[AMOUNT]&addInfo=[CONTENT]
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact.png?amount=%.0f&addInfo=%s",
                sepayBankCode,
                sepayAccountNumber,
                amount,
                content
        );
    }

    private boolean verifySignature(SepayWebhookRequest request) {
        // TODO: Implement real signature verification
        // String data = request.getTransactionId() + request.getAmount() + request.getContent() + sepaySecretKey;
        // String calculatedSignature = DigestUtils.sha256Hex(data);
        // return calculatedSignature.equals(request.getSignature());

        // For demo, always return true
        return true;
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .bankCode(payment.getSepayBankCode())
                .accountNumber(payment.getSepayAccountNumber())
                .accountName(payment.getSepayAccountName())
                .content(payment.getSepayContent())
                .qrCodeUrl(payment.getSepayQrCode())
                .expiredAt(payment.getExpiredAt().toString())
                .build();
    }
}
