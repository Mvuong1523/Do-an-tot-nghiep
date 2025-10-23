package com.doan.WEB_TMDT.module.inventory.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;

public interface InventoryService {
    ApiResponse createProduct(CreateProductRequest req);
    ApiResponse createSupplier(CreateSupplierRequest req);
    ApiResponse importStock(ImportStockRequest req, String actorEmail);
    ApiResponse exportStock(ExportStockRequest req, String actorEmail);
    ApiResponse getStocks(); // đơn giản: list tất cả
}
