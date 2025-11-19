package com.doan.WEB_TMDT.module.inventory.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final com.doan.WEB_TMDT.module.inventory.service.ProductSpecificationService productSpecificationService;
    // ===== Products =====
    @PostMapping("/create_pchaseOrder")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse createPurchaseOrder(@Valid @RequestBody CreatePORequest req){
        return inventoryService.createPurchaseOrder(req);
    }
    // ===== Suppliers =====
    @PostMapping("/suppliers")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse createSupplier(@Valid @RequestBody CreateSupplierRequest req) {
        return inventoryService.getOrCreateSupplier(req);
    }

    @GetMapping("/supplier/{supplierId}/products")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse getProductsBySupplier(@PathVariable Long supplierId) {
        List<Product> products = productRepository.findAllByWarehouseProduct_Supplier_Id(supplierId);
        return ApiResponse.success("Danh sách sản phẩm", products);
    }
    // ===== Import / Export =====
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse importStock(@Valid @RequestBody CompletePORequest req, Authentication auth) {
        String actor = auth != null ? auth.getName() : "system";
        return inventoryService.completePurchaseOrder(req);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse export(@RequestBody CreateExportOrderRequest req) {
        return inventoryService.createExportOrder(req);
    }

    // ===== Search by Specifications =====
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse searchBySpecs(@RequestParam String keyword) {
        var products = productSpecificationService.searchBySpecValue(keyword);
        return ApiResponse.success("Tìm thấy " + products.size() + " sản phẩm", products);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse filterBySpecs(
            @RequestParam String key,
            @RequestParam String value
    ) {
        var products = productSpecificationService.searchBySpecKeyAndValue(key, value);
        return ApiResponse.success("Tìm thấy " + products.size() + " sản phẩm", products);
    }

    // ===== Stock View (PRODUCT_MANAGER có thể xem, nhưng chỉ đọc) =====
    @GetMapping("/stock")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse getStocks() {
        return inventoryService.getStocks();
    }

    // ===== Transaction History =====
    @GetMapping("/purchase-orders")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse getPurchaseOrders(@RequestParam(required = false) String status) {
        com.doan.WEB_TMDT.module.inventory.entity.POStatus poStatus = null;
        if (status != null && !status.isEmpty()) {
            poStatus = com.doan.WEB_TMDT.module.inventory.entity.POStatus.valueOf(status);
        }
        return inventoryService.getPurchaseOrders(poStatus);
    }

    @GetMapping("/export-orders")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse getExportOrders(@RequestParam(required = false) String status) {
        com.doan.WEB_TMDT.module.inventory.entity.ExportStatus exportStatus = null;
        if (status != null && !status.isEmpty()) {
            exportStatus = com.doan.WEB_TMDT.module.inventory.entity.ExportStatus.valueOf(status);
        }
        return inventoryService.getExportOrders(exportStatus);
    }

    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse getPurchaseOrderDetail(@PathVariable Long id) {
        return inventoryService.getPurchaseOrderDetail(id);
    }

    @GetMapping("/export-orders/{id}")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse getExportOrderDetail(@PathVariable Long id) {
        return inventoryService.getExportOrderDetail(id);
    }

    @PutMapping("/purchase-orders/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse cancelPurchaseOrder(@PathVariable Long id) {
        return inventoryService.cancelPurchaseOrder(id);
    }

    @PutMapping("/export-orders/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
    public ApiResponse cancelExportOrder(@PathVariable Long id) {
        return inventoryService.cancelExportOrder(id);
    }
}
