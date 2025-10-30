package com.doan.WEB_TMDT.module.inventory.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ===== Products =====
    @PostMapping("/products")
    public ApiResponse createProduct(@RequestBody CreateProductRequest req) {
        return inventoryService.createProduct(req);
    }

    // ===== Suppliers =====
    @PostMapping("/suppliers")
    public ApiResponse createSupplier(@RequestBody CreateSupplierRequest req) {
        return inventoryService.createSupplier(req);
    }

    // ===== Import / Export =====
    @PostMapping("/import")
    public ApiResponse importStock(@RequestBody ImportStockRequest req, Authentication auth) {
        String actor = auth != null ? auth.getName() : "system";
        return inventoryService.importStock(req, actor);
    }

    @PostMapping("/export")
    public ApiResponse exportStock(@RequestBody ExportStockRequest req, Authentication auth) {
        String actor = auth != null ? auth.getName() : "system";
        return inventoryService.exportStock(req, actor);
    }

    // ===== Stock View =====
    @GetMapping("/stock")
    public ApiResponse getStocks() {
        return inventoryService.getStocks();
    }
}
