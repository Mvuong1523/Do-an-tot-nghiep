package com.doan.WEB_TMDT.module.auth.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse registerCustomer(@RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam String fullName,
                                        @RequestParam String phone,
                                        @RequestParam String address) {
        return userService.registerCustomer(email, password, fullName, phone, address);
    }
}
