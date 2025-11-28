package com.doan.WEB_TMDT.module.auth.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.CustomerRepository;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
public class CustomerController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /**
     * Lấy thông tin profile của customer đang đăng nhập
     */
    @GetMapping("/profile")
    public ApiResponse getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
            
            return ApiResponse.success("Thông tin khách hàng", customer);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin profile
     */
    @PutMapping("/profile")
    public ApiResponse updateProfile(@RequestBody Customer updateData, Authentication authentication) {
        try {
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
            
            // Cập nhật thông tin
            if (updateData.getFullName() != null) {
                customer.setFullName(updateData.getFullName());
            }
            if (updateData.getPhone() != null) {
                customer.setPhone(updateData.getPhone());
            }
            if (updateData.getAddress() != null) {
                customer.setAddress(updateData.getAddress());
            }
            if (updateData.getGender() != null) {
                customer.setGender(updateData.getGender());
            }
            if (updateData.getBirthDate() != null) {
                customer.setBirthDate(updateData.getBirthDate());
            }
            
            customerRepository.save(customer);
            
            return ApiResponse.success("Cập nhật thông tin thành công", customer);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
