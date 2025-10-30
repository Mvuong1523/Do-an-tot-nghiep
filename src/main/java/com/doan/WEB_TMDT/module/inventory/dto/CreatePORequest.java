package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatePORequest {
    private String supplierName;
    private String supplierId;
    private List<POItemRequest> items;
    private String note;
}
