package com.doan.WEB_TMDT.module.shipping.helper;

import com.doan.WEB_TMDT.module.shipping.constants.ShippingConstants;
import org.springframework.stereotype.Component;

@Component
public class HanoiInnerCityChecker {

    public boolean isHanoiInnerCity(String province, String district) {
        if (province == null || district == null) {
            return false;
        }

        String normalizedProvince = province.trim().toLowerCase();
        String normalizedDistrict = district.trim();

        boolean isHanoi = normalizedProvince.contains("hà nội") || 
                         normalizedProvince.contains("ha noi") ||
                         normalizedProvince.equals("hanoi");

        if (!isHanoi) {
            return false;
        }

        return ShippingConstants.HANOI_INNER_DISTRICTS.stream()
                .anyMatch(innerDistrict -> normalizedDistrict.contains(innerDistrict));
    }
}
