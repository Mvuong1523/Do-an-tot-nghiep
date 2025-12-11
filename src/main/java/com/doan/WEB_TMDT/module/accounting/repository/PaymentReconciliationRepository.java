package com.doan.WEB_TMDT.module.accounting.repository;

import com.doan.WEB_TMDT.module.accounting.entity.PaymentReconciliation;
import com.doan.WEB_TMDT.module.accounting.entity.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentReconciliationRepository extends JpaRepository<PaymentReconciliation, Long> {
    List<PaymentReconciliation> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<PaymentReconciliation> findByGatewayAndTransactionDateBetween(
            String gateway, LocalDateTime start, LocalDateTime end);
    
    long countByStatus(ReconciliationStatus status);
    
    @Query("SELECT SUM(p.discrepancy) FROM PaymentReconciliation p WHERE p.status = :status")
    Double sumDiscrepancyByStatus(@Param("status") ReconciliationStatus status);
    
    @Query("SELECT SUM(p.discrepancy) FROM PaymentReconciliation p WHERE p.transactionDate BETWEEN :startDate AND :endDate")
    Double sumDiscrepancyByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
