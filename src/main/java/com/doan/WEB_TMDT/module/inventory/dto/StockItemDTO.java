package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockItemDTO {
    private Long productId;
    private Long quantity;
    private Long unitCost;
    private List<String> serialNumbers; // ✅ danh sách serial nhập kho
}
