package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class CreateSupplierRequest {
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private String bankAccount;
    private String paymentTerm; // điều khoản thanh toán (text mô tả)
    private Integer paymentTermDays; // Số ngày nợ (VD: 30, 60, 90)
    @Column(nullable = false)
    private Boolean active = true;
}
