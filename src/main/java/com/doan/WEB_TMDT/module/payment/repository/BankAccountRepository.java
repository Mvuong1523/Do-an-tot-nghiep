package com.doan.WEB_TMDT.module.payment.repository;

import com.doan.WEB_TMDT.module.payment.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByIsActiveTrue();
    Optional<BankAccount> findByIsDefaultTrue();
    List<BankAccount> findAllByOrderByIsDefaultDescCreatedAtDesc();
}
