package com.doan.WEB_TMDT.module.auth.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.Position;
import com.doan.WEB_TMDT.module.auth.service.EmployeeRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee-registration")
@RequiredArgsConstructor
public class EmployeeRegistrationController {

    private final EmployeeRegistrationService registrationService;

    // 🟡 Nhân viên gửi yêu cầu đăng ký
    @PostMapping("/apply")
    public ApiResponse applyEmployee(@RequestParam String fullName,
                                     @RequestParam String email,
                                     @RequestParam String phone,
                                     @RequestParam String address,
                                     @RequestParam Position position,
                                     @RequestParam(required = false) String note) {
        return registrationService.registerEmployee(fullName, email, phone, address, position, note);
    }

    // 🟢 Admin duyệt yêu cầu
    @PostMapping("/approve/{id}")
    public ApiResponse approveEmployee(@PathVariable Long id) {
        return registrationService.approveEmployee(id);
    }
}
