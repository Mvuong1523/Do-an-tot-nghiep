package com.doan.WEB_TMDT.module.payment.repository;

import com.doan.WEB_TMDT.module.payment.entity.Payment;
import com.doan.WEB_TMDT.module.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentCode(String paymentCode);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findBySepayTransactionId(String transactionId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusAndExpiredAtBefore(PaymentStatus status, LocalDateTime expiredAt);
    boolean existsByPaymentCode(String paymentCode);
}
