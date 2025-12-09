package com.doan.WEB_TMDT.config;

import com.doan.WEB_TMDT.module.payment.entity.BankAccount;
import com.doan.WEB_TMDT.module.payment.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankAccountDataInitializer implements CommandLineRunner {

    private final BankAccountRepository bankAccountRepository;

    @Value("${sepay.bank.code:MBBank}")
    private String bankCode;

    @Value("${sepay.bank.account.number:3333315012003}")
    private String accountNumber;

    @Value("${sepay.bank.account.name:LE MINH VUONG}")
    private String accountName;

    @Override
    public void run(String... args) {
        try {
            // Kiểm tra xem đã có tài khoản nào chưa
            long count = bankAccountRepository.count();
            
            if (count == 0) {
                log.info("🏦 Initializing default bank account from application.properties...");
                
                // Tạo tài khoản mặc định từ config
                BankAccount defaultAccount = BankAccount.builder()
                        .bankCode(bankCode)
                        .bankName(getBankName(bankCode))
                        .accountNumber(accountNumber)
                        .accountName(accountName)
                        .description("Tài khoản mặc định (từ config)")
                        .isActive(true)
                        .isDefault(true)
                        .build();
                
                bankAccountRepository.save(defaultAccount);
                
                log.info("✅ Default bank account created: {} - {} - {}", 
                    bankCode, accountNumber, accountName);
            } else {
                log.info("ℹ️ Bank accounts already exist, skipping initialization");
            }
        } catch (Exception e) {
            log.error("❌ Error initializing bank account: {}", e.getMessage());
        }
    }
    
    private String getBankName(String code) {
        switch (code.toUpperCase()) {
            case "MBBANK": return "MB Bank";
            case "VCB": return "Vietcombank";
            case "TCB": return "Techcombank";
            case "ACB": return "ACB";
            case "VPBANK": return "VPBank";
            case "VIETINBANK": return "Vietinbank";
            case "BIDV": return "BIDV";
            case "AGRIBANK": return "Agribank";
            case "SACOMBANK": return "Sacombank";
            case "HDBANK": return "HDBank";
            default: return code;
        }
    }
}
