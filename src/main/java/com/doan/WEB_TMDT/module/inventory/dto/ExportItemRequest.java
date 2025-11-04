package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExportItemRequest {
    @NotBlank private String productSku;              // Mã SKU sản phẩm
    @NotEmpty
    private List< @NotBlank String> serialNumbers;     // Danh sách serial được xuất
}