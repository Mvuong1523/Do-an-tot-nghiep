package com.doan.WEB_TMDT.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.accounting.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final AccountingService accountingService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ACCOUNTANT', 'SALE', 'SALES', 'WAREHOUSE', 'PRODUCT_MANAGER', 'CSKH', 'SHIPPER')")
    public ApiResponse getDashboardStats() {
        return accountingService.getDashboardStats();
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ACCOUNTANT', 'SALE', 'SALES', 'WAREHOUSE', 'PRODUCT_MANAGER', 'CSKH', 'SHIPPER')")
    public ApiResponse getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        return accountingService.getRecentOrders(limit);
    }
}
