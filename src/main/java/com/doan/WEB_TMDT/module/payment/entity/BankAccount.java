package com.doan.WEB_TMDT.module.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String bankCode; // MBBank, VCB, TCB, ACB...
    
    @Column(nullable = false)
    private String bankName; // MB Bank, Vietcombank...
    
    @Column(nullable = false)
    private String accountNumber;
    
    @Column(nullable = false)
    private String accountName;
    
    @Column(length = 500)
    private String description; // Ghi chú
    
    @Column(length = 255)
    private String sepayApiToken; // API token từ SePay (nếu có)
    
    @Column(length = 100)
    private String sepayMerchantId; // Merchant ID từ SePay (nếu có)
    
    @Column(nullable = false)
    private Boolean isActive = false; // Tài khoản đang dùng
    
    @Column(nullable = false)
    private Boolean isDefault = false; // Tài khoản mặc định
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
