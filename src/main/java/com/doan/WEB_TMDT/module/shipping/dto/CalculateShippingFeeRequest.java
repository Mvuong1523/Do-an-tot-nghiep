package com.doan.WEB_TMDT.module.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateShippingFeeRequest {
    private String province; // Tỉnh/Thành phố
    private String district; // Quận/Huyện
    private String ward;     // Phường/Xã
    private String address;  // Địa chỉ cụ thể
    private Double weight;   // Khối lượng (gram)
    private Double value;    // Giá trị đơn hàng
}
