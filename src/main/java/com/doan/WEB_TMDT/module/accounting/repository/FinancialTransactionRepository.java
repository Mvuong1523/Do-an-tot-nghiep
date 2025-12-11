package com.doan.WEB_TMDT.module.accounting.repository;

import com.doan.WEB_TMDT.module.accounting.entity.FinancialTransaction;
import com.doan.WEB_TMDT.module.accounting.entity.TransactionCategory;
import com.doan.WEB_TMDT.module.accounting.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    
    List<FinancialTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<FinancialTransaction> findByTypeAndTransactionDateBetween(
            TransactionType type, LocalDateTime startDate, LocalDateTime endDate);
    
    List<FinancialTransaction> findByCategoryAndTransactionDateBetween(
            TransactionCategory category, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(f.amount) FROM FinancialTransaction f WHERE f.type = :type AND f.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(f.amount) FROM FinancialTransaction f WHERE f.category = :category AND f.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByCategoryAndDateRange(
            @Param("category") TransactionCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT f.category, SUM(f.amount) FROM FinancialTransaction f WHERE f.type = :type AND f.transactionDate BETWEEN :startDate AND :endDate GROUP BY f.category")
    List<Object[]> sumAmountByTypeAndCategoryAndDateRange(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}