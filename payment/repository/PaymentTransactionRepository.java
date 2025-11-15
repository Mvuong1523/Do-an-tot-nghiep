package com.doan.WEB_TMDT.module.payment.repository;

import com.doan.WEB_TMDT.module.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByPaymentCode(String code);
}
