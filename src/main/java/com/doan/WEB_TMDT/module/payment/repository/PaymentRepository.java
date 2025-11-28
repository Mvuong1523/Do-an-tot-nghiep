package com.doan.WEB_TMDT.module.payment.repository;

import com.doan.WEB_TMDT.module.payment.entity.Payment;
import com.doan.WEB_TMDT.module.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Accounting queries
    List<Payment> findByPaidAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.paidAt BETWEEN :startDate AND :endDate " +
           "AND p.status = com.doan.WEB_TMDT.module.payment.entity.PaymentStatus.SUCCESS")
    List<Payment> findSuccessfulPaymentsBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paidAt BETWEEN :startDate AND :endDate " +
           "AND p.status = com.doan.WEB_TMDT.module.payment.entity.PaymentStatus.SUCCESS")
    Double sumAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
}
