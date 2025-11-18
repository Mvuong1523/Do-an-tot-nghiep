package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetailResponse {
    private Long id;
    private String poCode;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime receivedDate;
    private String createdBy;
    private String note;
    private SupplierInfo supplier;
    private List<PurchaseOrderItemInfo> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierInfo {
        private Long id;
        private String name;
        private String taxCode;
        private String phone;
        private String email;
        private String address;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderItemInfo {
        private Long id;
        private String sku;
        private Integer quantity;
        private Double unitCost;
        private Integer warrantyMonths;
        private String note;
        private WarehouseProductInfo warehouseProduct;
        private List<ProductDetailInfo> productDetails;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseProductInfo {
        private Long id;
        private String sku;
        private String internalName;
        private String description;
        private String techSpecsJson;  // Thông số kỹ thuật
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDetailInfo {
        private Long id;
        private String serialNumber;
        private Double importPrice;
        private LocalDateTime importDate;
        private String status;
        private Integer warrantyMonths;
    }
}
