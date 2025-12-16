package com.doan.WEB_TMDT.module.accounting.repository;

import com.doan.WEB_TMDT.module.accounting.entity.PayableStatus;
import com.doan.WEB_TMDT.module.accounting.entity.SupplierPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SupplierPayableRepository extends JpaRepository<SupplierPayable, Long> {
    
    Optional<SupplierPayable> findByPayableCode(String payableCode);
    
    Optional<SupplierPayable> findByPurchaseOrderId(Long purchaseOrderId);
    
    List<SupplierPayable> findBySupplierId(Long supplierId);
    
    List<SupplierPayable> findByStatus(PayableStatus status);
    
    // Tìm công nợ quá hạn
    @Query("SELECT sp FROM SupplierPayable sp WHERE sp.dueDate < :currentDate AND sp.status != 'PAID'")
    List<SupplierPayable> findOverduePayables(@Param("currentDate") LocalDate currentDate);
    
    // Tìm công nợ sắp đến hạn (trong X ngày tới)
    @Query("SELECT sp FROM SupplierPayable sp WHERE sp.dueDate BETWEEN :startDate AND :endDate AND sp.status != 'PAID'")
    List<SupplierPayable> findUpcomingPayables(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Tổng công nợ theo nhà cung cấp
    @Query("SELECT SUM(sp.remainingAmount) FROM SupplierPayable sp WHERE sp.supplier.id = :supplierId AND sp.status != 'PAID'")
    BigDecimal getTotalPayableBySupplier(@Param("supplierId") Long supplierId);
    
    // Tổng công nợ toàn bộ
    @Query("SELECT SUM(sp.remainingAmount) FROM SupplierPayable sp WHERE sp.status != 'PAID'")
    BigDecimal getTotalOutstandingPayables();
    
    // Công nợ theo khoảng thời gian
    List<SupplierPayable> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);
}
