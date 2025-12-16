package com.doan.WEB_TMDT.module.accounting.repository;

import com.doan.WEB_TMDT.module.accounting.entity.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
    
    Optional<SupplierPayment> findByPaymentCode(String paymentCode);
    
    List<SupplierPayment> findByPayableId(Long payableId);
    
    List<SupplierPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Tổng thanh toán theo công nợ
    @Query("SELECT SUM(sp.amount) FROM SupplierPayment sp WHERE sp.payable.id = :payableId")
    BigDecimal getTotalPaymentByPayable(@Param("payableId") Long payableId);
    
    // Tổng thanh toán trong khoảng thời gian
    @Query("SELECT SUM(sp.amount) FROM SupplierPayment sp WHERE sp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaymentInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
