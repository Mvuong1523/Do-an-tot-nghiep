package com.doan.WEB_TMDT.module.payment.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.payment.dto.BankAccountRequest;
import com.doan.WEB_TMDT.module.payment.dto.BankAccountResponse;
import com.doan.WEB_TMDT.module.payment.entity.BankAccount;
import com.doan.WEB_TMDT.module.payment.repository.BankAccountRepository;
import com.doan.WEB_TMDT.module.payment.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    @Override
    public ApiResponse getAll() {
        List<BankAccount> accounts = bankAccountRepository.findAllByOrderByIsDefaultDescCreatedAtDesc();
        List<BankAccountResponse> responses = accounts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Danh sách tài khoản ngân hàng", responses);
    }

    @Override
    public ApiResponse getById(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ngân hàng"));
        return ApiResponse.success("Chi tiết tài khoản", toResponse(account));
    }

    @Override
    public ApiResponse getDefault() {
        BankAccount account = bankAccountRepository.findByIsDefaultTrue()
                .orElse(null);
        if (account == null) {
            return ApiResponse.error("Chưa có tài khoản mặc định");
        }
        return ApiResponse.success("Tài khoản mặc định", toResponse(account));
    }

    @Override
    @Transactional
    public ApiResponse create(BankAccountRequest request) {
        BankAccount account = BankAccount.builder()
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .description(request.getDescription())
                .sepayApiToken(request.getSepayApiToken())
                .sepayMerchantId(request.getSepayMerchantId())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        // Nếu set làm default, bỏ default của các tài khoản khác
        if (account.getIsDefault()) {
            bankAccountRepository.findByIsDefaultTrue().ifPresent(existing -> {
                existing.setIsDefault(false);
                bankAccountRepository.save(existing);
            });
        }

        BankAccount saved = bankAccountRepository.save(account);
        log.info("Created bank account: {} - {}", saved.getBankName(), saved.getAccountNumber());
        
        return ApiResponse.success("Tạo tài khoản ngân hàng thành công", toResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse update(Long id, BankAccountRequest request) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ngân hàng"));

        account.setBankCode(request.getBankCode());
        account.setBankName(request.getBankName());
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountName(request.getAccountName());
        account.setDescription(request.getDescription());
        account.setSepayApiToken(request.getSepayApiToken());
        account.setSepayMerchantId(request.getSepayMerchantId());
        
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }
        
        if (request.getIsDefault() != null && request.getIsDefault()) {
            // Bỏ default của các tài khoản khác
            bankAccountRepository.findByIsDefaultTrue().ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    existing.setIsDefault(false);
                    bankAccountRepository.save(existing);
                }
            });
            account.setIsDefault(true);
        }

        BankAccount updated = bankAccountRepository.save(account);
        log.info("Updated bank account: {}", id);
        
        return ApiResponse.success("Cập nhật tài khoản thành công", toResponse(updated));
    }

    @Override
    @Transactional
    public ApiResponse delete(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ngân hàng"));

        if (account.getIsDefault()) {
            return ApiResponse.error("Không thể xóa tài khoản mặc định");
        }

        bankAccountRepository.delete(account);
        log.info("Deleted bank account: {}", id);
        
        return ApiResponse.success("Xóa tài khoản thành công");
    }

    @Override
    @Transactional
    public ApiResponse setDefault(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ngân hàng"));

        // Bỏ default của tài khoản khác
        bankAccountRepository.findByIsDefaultTrue().ifPresent(existing -> {
            existing.setIsDefault(false);
            bankAccountRepository.save(existing);
        });

        // Set default cho tài khoản này
        account.setIsDefault(true);
        account.setIsActive(true); // Tự động active khi set default
        bankAccountRepository.save(account);
        
        log.info("Set default bank account: {}", id);
        
        return ApiResponse.success("Đã đặt làm tài khoản mặc định", toResponse(account));
    }

    @Override
    @Transactional
    public ApiResponse toggleActive(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ngân hàng"));

        account.setIsActive(!account.getIsActive());
        bankAccountRepository.save(account);
        
        String status = account.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
        log.info("Toggled bank account {}: {}", id, status);
        
        return ApiResponse.success("Đã " + status + " tài khoản", toResponse(account));
    }

    private BankAccountResponse toResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankCode(account.getBankCode())
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .description(account.getDescription())
                .sepayApiToken(account.getSepayApiToken())
                .sepayMerchantId(account.getSepayMerchantId())
                .isActive(account.getIsActive())
                .isDefault(account.getIsDefault())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
