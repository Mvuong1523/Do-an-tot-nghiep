package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImportStockRequest {
    private String supplierId; // hoặc supplierId tuỳ bạn
    private String note;
    private List<StockItemDTO> items;
}
