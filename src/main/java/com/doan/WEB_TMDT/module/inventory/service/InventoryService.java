package com.doan.WEB_TMDT.module.inventory.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;

public interface InventoryService {

    ApiResponse getOrCreateSupplier(CreateSupplierRequest req);

    ApiResponse createPurchaseOrder(CreatePORequest req);

    ApiResponse completePurchaseOrder(CompletePORequest req);

    ApiResponse createExportOrder(CreateExportOrderRequest req);

    // ================================
    //        HÀM MỚI BẠN YÊU CẦU
    // ================================

    /**
     * Xuất kho để bán hàng (xuất theo SKU và số lượng)
     */
    ApiResponse exportForSale(ExportInventoryRequest req);

    /**
     * Xuất kho để bảo hành (xuất đúng 1 serial cụ thể)
     */
    ApiResponse exportForWarranty(WarrantyExportRequest req);
}