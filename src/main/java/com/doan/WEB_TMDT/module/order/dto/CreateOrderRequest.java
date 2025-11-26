package com.doan.WEB_TMDT.module.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    // Thông tin giao hàng
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String province;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;
    
    // Phường/Xã không bắt buộc (có thể để trống)
    private String ward;
    
    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String address;
    
    private String note; // Ghi chú
    
    // Phí vận chuyển (đã tính từ frontend)
    @NotNull(message = "Phí vận chuyển không được để trống")
    private Double shippingFee;
}
