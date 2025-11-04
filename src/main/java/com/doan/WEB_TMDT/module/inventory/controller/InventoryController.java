package com.doan.WEB_TMDT.module.inventory.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.entity.PurchaseOrder;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private  final ProductRepository productRepository;
    // ===== Products =====
    @PostMapping("/create_pchaseOrder")
    public  ApiResponse createPurchaseOrder(@RequestBody CreatePORequest req){
        return inventoryService.createPurchaseOrder(req);
    }
    // ===== Suppliers =====
    @PostMapping("/suppliers")
    public ApiResponse createSupplier(@RequestBody CreateSupplierRequest req) {
        return inventoryService.getOrCreateSupplier(req);
    }

    @GetMapping("/supplier/{supplierId}/products")
    public ApiResponse getProductsBySupplier(@PathVariable Long supplierId) {
        List<WarehouseProduct> products = productRepository.findAllBySupplier_Id(supplierId);
        return ApiResponse.success("Danh sách sản phẩm", products);
    }
    // ===== Import / Export =====
    @PostMapping("/import")
    public ApiResponse importStock(@RequestBody CompletePORequest req, Authentication auth) {
        String actor = auth != null ? auth.getName() : "system";
        return inventoryService.completePurchaseOrder(req);
    }

    @PostMapping("/export")
    public ApiResponse exportStock(@RequestBody ExportInventoryRequest req, Authentication auth) {
        String actor = auth != null ? auth.getName() : "system";
        return inventoryService.exportInventory(req);
    }

    @PostMapping("/create")
    public ApiResponse export(@RequestBody CreateExportOrderRequest req) {
        return inventoryService.createExportOrder(req); // ✅ gọi đúng
    }

    // ===== Stock View =====
//    @GetMapping("/stock")
//    public ApiResponse getStocks() {
//        return inventoryService.getStocks();
//    }
}
