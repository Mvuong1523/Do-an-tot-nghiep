package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean autoCreated = false;


    @Column(nullable = false, length = 255)
    private String name;
    private  String contactName;
    private String phone;
    private String email;
    private String address;
    @Column(name = "tax_code", nullable = false, unique = true)
    private String taxCode;
    private String bankAccount;
    private String paymentTerm; // điều khoản thanh toán (text mô tả)
    
    @Column(name = "payment_term_days")
    private Integer paymentTermDays; // Số ngày nợ (VD: 30, 60, 90 ngày)
    
    @Column(nullable = false)
    private Boolean active = true;
}
