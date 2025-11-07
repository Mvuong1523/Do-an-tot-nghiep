package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreatePORequest {
    @NotBlank
    private String createdBy;

    @NotBlank
    private String poCode;

    @Valid
    private CreateSupplierRequest supplier; // nếu muốn tạo NCC mới ngay


    private String supplierId;
    @NotEmpty
    @Valid
    private List<POItemRequest> items;
    private String note;
}
