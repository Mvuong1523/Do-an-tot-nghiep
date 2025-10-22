package com.doan.WEB_TMDT.module.auth.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.common.dto.auth.LoginRequest;

public interface UserService {
    ApiResponse registerCustomer(String email, String password, String fullName, String phone, String address);
    ApiResponse login(LoginRequest request);
}
