package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExportStockRequest {
    private String refOrderCode; // mã đơn hàng (tham chiếu)
    private String note;
    private List<StockItemDTO> items; // productId + quantity (unitCost có thể bỏ)
}
