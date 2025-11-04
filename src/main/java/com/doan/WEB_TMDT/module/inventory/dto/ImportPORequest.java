package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportPORequest {
    @Valid
    @NotNull
    private CreateSupplierRequest supplier;
    @NotBlank
    private String createdBy;
    private String note;
    @NotEmpty
    @Valid private List<POItemRequest> items; // sku có thể chưa tồn tại
}

