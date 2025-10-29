package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

@Data
public class CreateSupplierRequest {
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
}
