package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExportItemRequest {
    private String productSku;              // Mã SKU sản phẩm
    private List<String> serialNumbers;     // Danh sách serial được xuất
}