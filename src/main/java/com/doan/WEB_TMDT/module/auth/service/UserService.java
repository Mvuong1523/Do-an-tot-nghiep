package com.doan.WEB_TMDT.module.auth.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;

public interface UserService {
    ApiResponse registerCustomer(String email, String password, String fullName, String phone, String address);
}
