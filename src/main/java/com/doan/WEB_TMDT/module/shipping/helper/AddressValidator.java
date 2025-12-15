package com.doan.WEB_TMDT.module.shipping.helper;

import org.springframework.stereotype.Component;

@Component
public class AddressValidator {
    
    /**
     * Validate địa chỉ giao hàng đầy đủ
     */
    public boolean isValidAddress(String province, String district, String ward, String address) {
        return province != null && !province.trim().isEmpty() &&
               district != null && !district.trim().isEmpty() &&
               ward != null && !ward.trim().isEmpty() &&
               address != null && !address.trim().isEmpty();
    }
    
    /**
     * Validate địa chỉ cơ bản (không cần ward)
     */
    public boolean isValidBasicAddress(String province, String district, String address) {
        return province != null && !province.trim().isEmpty() &&
               district != null && !district.trim().isEmpty() &&
               address != null && !address.trim().isEmpty();
    }
}
