package com.doan.WEB_TMDT.module.inventory.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;

import java.time.LocalDateTime;

public interface InventoryService {
    ApiResponse createPurchaseOrder(CreatePORequest req, String actor);
    ApiResponse addSerialToPO(Long poId, String serial, String productSku);
    ApiResponse completePO(Long poId, LocalDateTime receivedDate);
    ApiResponse performStockAudit(StockAuditRequest req, String actor);
}
