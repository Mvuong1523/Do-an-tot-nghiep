package com.doan.WEB_TMDT.module.auth.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.dto.EmployeeRegistrationRequest;
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
    public ApiResponse registerEmployee(@RequestBody EmployeeRegistrationRequest req) {
        System.out.println("=== CONTROLLER RECEIVED REQUEST ===");
        System.out.println("Request: " + req);
        System.out.println("Full Name: " + req.getFullName());
        System.out.println("Email: " + req.getEmail());
        System.out.println("Phone: " + req.getPhone());
        System.out.println("Position: " + req.getPosition());
        
        ApiResponse response = registrationService.registerEmployee(
                req.getFullName(),
                req.getEmail(),
                req.getPhone(),
                req.getAddress(),
                req.getPosition(),
                req.getNote()
        );
        
        System.out.println("Response: " + response);
        return response;
    }

    // 🟢 Admin duyệt yêu cầu
    @PostMapping("/approve/{id}")
    public ApiResponse approveEmployee(@PathVariable Long id) {
        return registrationService.approveEmployee(id);
    }

    // 📋 Admin xem danh sách đăng ký
    @GetMapping("/list")
    public ApiResponse getAllRegistrations() {
        return registrationService.getAllRegistrations();
    }

    // 📋 Admin xem danh sách chờ duyệt
    @GetMapping("/pending")
    public ApiResponse getPendingRegistrations() {
        return registrationService.getPendingRegistrations();
    }

    // 🔍 Debug endpoint - kiểm tra database
    @GetMapping("/debug/count")
    public ApiResponse getRegistrationCount() {
        long count = registrationService.getRegistrationCount();
        System.out.println("Total registrations in DB: " + count);
        return ApiResponse.success("Total registrations: " + count, count);
    }
}
