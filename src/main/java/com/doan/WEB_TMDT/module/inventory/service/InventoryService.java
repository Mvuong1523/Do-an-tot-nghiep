package com.doan.WEB_TMDT.module.inventory.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;

import java.time.LocalDateTime;

public interface InventoryService {
    ApiResponse createSupplier(CreateSupplierRequest req);
    ApiResponse createPurchaseOrder(CreatePORequest req);
    ApiResponse completePurchaseOrder(CompletePORequest req);
    ApiResponse exportInventory(ExportInventoryRequest req);
}
