package com.doan.WEB_TMDT.module.accounting.repository;

import com.doan.WEB_TMDT.module.accounting.entity.TaxReport;
import com.doan.WEB_TMDT.module.accounting.entity.TaxReportStatus;
import com.doan.WEB_TMDT.module.accounting.entity.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxReportRepository extends JpaRepository<TaxReport, Long> {
    
    List<TaxReport> findByTaxTypeOrderByPeriodStartDesc(TaxType taxType);
    
    List<TaxReport> findByStatusOrderByPeriodStartDesc(TaxReportStatus status);
    
    Optional<TaxReport> findByTaxTypeAndPeriodStartAndPeriodEnd(
            TaxType taxType, LocalDate periodStart, LocalDate periodEnd);
    
    @Query("SELECT SUM(t.taxAmount) FROM TaxReport t WHERE t.taxType = :taxType AND t.status = :status")
    BigDecimal sumTaxAmountByTypeAndStatus(
            @Param("taxType") TaxType taxType,
            @Param("status") TaxReportStatus status);
    
    @Query("SELECT SUM(t.remainingTax) FROM TaxReport t WHERE t.taxType = :taxType AND t.remainingTax > 0")
    BigDecimal sumRemainingTaxByType(@Param("taxType") TaxType taxType);
}