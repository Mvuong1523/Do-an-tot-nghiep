package com.doan.WEB_TMDT.module.auth.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.dto.FirstChangePasswordRequest;
import com.doan.WEB_TMDT.module.auth.dto.LoginRequest;
import com.doan.WEB_TMDT.module.auth.dto.ChangePasswordRequest;

public interface UserService {
    ApiResponse registerCustomer(String email, String password, String fullName, String phone, String address);
    ApiResponse login(LoginRequest request);
    ApiResponse changePassword(String email, ChangePasswordRequest request);
    ApiResponse firstChangePassword(FirstChangePasswordRequest request);
    ApiResponse getCurrentUser(String email);
}
