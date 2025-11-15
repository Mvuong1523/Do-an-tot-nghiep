package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;
import java.util.List;

@Data
public class ShopOrderRequest {
    private String orderCode;

    // người nhận
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String customerProvince;
    private String customerDistrict;
    private String customerWard;

    private Integer codAmount; // tiền thu hộ
    private List<ShopOrderItem> items;
}
