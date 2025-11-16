package com.doan.WEB_TMDT.module.auth.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.EmployeeRegistration;
import com.doan.WEB_TMDT.module.auth.entity.Position;
import com.doan.WEB_TMDT.module.auth.repository.EmployeeRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final EmployeeRegistrationRepository registrationRepo;

    @GetMapping("/employee-registrations")
    public ApiResponse getAllRegistrations() {
        List<EmployeeRegistration> all = registrationRepo.findAll();
        return ApiResponse.success("Total: " + all.size(), all);
    }

    @PostMapping("/create-test-registration")
    public ApiResponse createTestRegistration() {
        EmployeeRegistration test = EmployeeRegistration.builder()
                .fullName("Test User " + System.currentTimeMillis())
                .email("test" + System.currentTimeMillis() + "@example.com")
                .phone("0" + System.currentTimeMillis())
                .address("Test Address")
                .position(Position.SALE)
                .note("Test note")
                .approved(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        EmployeeRegistration saved = registrationRepo.save(test);
        return ApiResponse.success("Created test registration with ID: " + saved.getId(), saved);
    }

    @DeleteMapping("/clear-registrations")
    public ApiResponse clearAllRegistrations() {
        long count = registrationRepo.count();
        registrationRepo.deleteAll();
        return ApiResponse.success("Deleted " + count + " registrations", null);
    }
}
