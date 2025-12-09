package com.doan.WEB_TMDT.module.payment.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.payment.dto.BankAccountRequest;

public interface BankAccountService {
    ApiResponse getAll();
    ApiResponse getById(Long id);
    ApiResponse getDefault();
    ApiResponse create(BankAccountRequest request);
    ApiResponse update(Long id, BankAccountRequest request);
    ApiResponse delete(Long id);
    ApiResponse setDefault(Long id);
    ApiResponse toggleActive(Long id);
}
