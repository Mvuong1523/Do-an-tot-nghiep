package com.doan.WEB_TMDT.module.accounting.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.service.AccountingPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounting/periods")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountingPeriodController {

    private final AccountingPeriodService periodService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
    public ResponseEntity<ApiResponse> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
    public ResponseEntity<ApiResponse> getPeriodById(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriodById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
    public ResponseEntity<ApiResponse> createPeriod(
            @RequestParam String name,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return ResponseEntity.ok(periodService.createPeriod(name, startDate, endDate));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
    public ResponseEntity<ApiResponse> closePeriod(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String closedBy = authentication.getName();
        return ResponseEntity.ok(periodService.closePeriod(id, closedBy));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> reopenPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.reopenPeriod(id));
    }

    @PostMapping("/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
    public ResponseEntity<ApiResponse> calculatePeriodStats(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.calculatePeriodStats(id));
    }
}
