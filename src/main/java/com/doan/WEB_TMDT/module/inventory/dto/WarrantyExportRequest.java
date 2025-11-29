package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class WarrantyExportRequest {

    @NotBlank
    private String createdBy;           // Nhân viên kho thực hiện

    @NotBlank
    private String warrantyType;        

    @NotBlank
    private String reason;              // Lý do bảo hành (lỗi, đổi trả,…)

    private String note;                // Ghi chú thêm

    private Long warrantyTicketId;      // Mã phiếu bảo hành (nếu có)

    @NotEmpty
    @Valid
    private List<ExportItemRequest> items; // SKU + serial cần xuất
}