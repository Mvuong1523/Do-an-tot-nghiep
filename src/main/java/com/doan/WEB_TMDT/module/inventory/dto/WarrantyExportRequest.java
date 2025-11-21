package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

@Data
public class WarrantyExportRequest {
    private Long productDetailId;
    private String note;
}