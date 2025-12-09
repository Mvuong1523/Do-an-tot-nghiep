package com.doan.WEB_TMDT.module.payment.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.payment.dto.BankAccountRequest;
import com.doan.WEB_TMDT.module.payment.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bank-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ApiResponse getAll() {
        return bankAccountService.getAll();
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return bankAccountService.getById(id);
    }

    @GetMapping("/default")
    public ApiResponse getDefault() {
        return bankAccountService.getDefault();
    }

    @PostMapping
    public ApiResponse create(@Valid @RequestBody BankAccountRequest request) {
        return bankAccountService.create(request);
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody BankAccountRequest request) {
        return bankAccountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return bankAccountService.delete(id);
    }

    @PutMapping("/{id}/set-default")
    public ApiResponse setDefault(@PathVariable Long id) {
        return bankAccountService.setDefault(id);
    }

    @PutMapping("/{id}/toggle-active")
    public ApiResponse toggleActive(@PathVariable Long id) {
        return bankAccountService.toggleActive(id);
    }
}
