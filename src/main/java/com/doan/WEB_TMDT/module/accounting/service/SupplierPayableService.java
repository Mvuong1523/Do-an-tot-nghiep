package com.doan.WEB_TMDT.module.accounting.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.dto.CreatePaymentRequest;
import com.doan.WEB_TMDT.module.inventory.entity.PurchaseOrder;

import java.time.LocalDate;

public interface SupplierPayableService {
    
    /**
     * Tạo công nợ khi nhập hàng
     */
    ApiResponse createPayableFromPurchaseOrder(PurchaseOrder purchaseOrder);
    
    /**
     * Lấy danh sách tất cả công nợ
     */
    ApiResponse getAllPayables();
    
    /**
     * Lấy công nợ theo ID
     */
    ApiResponse getPayableById(Long id);
    
    /**
     * Lấy công nợ theo nhà cung cấp
     */
    ApiResponse getPayablesBySupplier(Long supplierId);
    
    /**
     * Lấy công nợ quá hạn
     */
    ApiResponse getOverduePayables();
    
    /**
     * Lấy công nợ sắp đến hạn (trong X ngày tới)
     */
    ApiResponse getUpcomingPayables(Integer days);
    
    /**
     * Thanh toán công nợ
     */
    ApiResponse makePayment(CreatePaymentRequest request);
    
    /**
     * Lấy lịch sử thanh toán của một công nợ
     */
    ApiResponse getPaymentHistory(Long payableId);
    
    /**
     * Thống kê công nợ
     */
    ApiResponse getPayableStats();
    
    /**
     * Báo cáo công nợ theo khoảng thời gian
     */
    ApiResponse getPayableReport(LocalDate startDate, LocalDate endDate);
}
