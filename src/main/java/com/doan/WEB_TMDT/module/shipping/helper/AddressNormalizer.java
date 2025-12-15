package com.doan.WEB_TMDT.module.shipping.helper;

import org.springframework.stereotype.Component;

@Component
public class AddressNormalizer {
    
    /**
     * Normalize tên tỉnh/thành phố
     * VD: "Thành phố Hà Nội" -> "Hà Nội"
     */
    public String normalizeProvince(String province) {
        if (province == null) return null;
        return province.trim()
                .replaceAll("^(Thành phố|Tỉnh)\\s+", "");
    }
    
    /**
     * Normalize tên quận/huyện
     * VD: "Quận Ba Đình" -> "Ba Đình"
     */
    public String normalizeDistrict(String district) {
        if (district == null) return null;
        return district.trim()
                .replaceAll("^(Quận|Huyện|Thành phố|Thị xã)\\s+", "");
    }
    
    /**
     * Normalize tên phường/xã
     * VD: "Phường Cống Vị" -> "Cống Vị"
     */
    public String normalizeWard(String ward) {
        if (ward == null) return null;
        return ward.trim()
                .replaceAll("^(Phường|Xã|Thị trấn)\\s+", "");
    }
    
    /**
     * Normalize toàn bộ địa chỉ
     */
    public String normalizeFullAddress(String address) {
        if (address == null) return null;
        return address.trim()
                .replaceAll("\\s+", " ");
    }
}
