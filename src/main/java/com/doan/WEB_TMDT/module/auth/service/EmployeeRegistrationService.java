package com.doan.WEB_TMDT.module.auth.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.Position;

public interface EmployeeRegistrationService {
    ApiResponse registerEmployee(String fullName, String email, String phone, String address, Position position, String note);
    ApiResponse approveEmployee(Long registrationId);
    ApiResponse getAllRegistrations();
    ApiResponse getPendingRegistrations();
}
