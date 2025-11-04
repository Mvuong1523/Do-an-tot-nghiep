package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateExportOrderRequest {
    @NotBlank
    private String createdBy;
    @NotBlank private String reason;
    private String note;

    @NotEmpty
    @Valid
    private List<ExportItemRequest> items;
}
