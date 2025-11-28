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
public class ExportOrderDetailResponse {
    private Long id;
    private String exportCode;
    private String status;
    private LocalDateTime exportDate;
    private String createdBy;
    private String reason;
    private String note;
    private List<ExportOrderItemInfo> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportOrderItemInfo {
        private Long id;
        private String sku;
        private Long quantity;
        private Double totalCost;
        private List<String> serialNumbers;
        private WarehouseProductInfo warehouseProduct;
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
        private String techSpecsJson;
    }
}
