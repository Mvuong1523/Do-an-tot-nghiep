package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductSerialRequest {
    private String productSku;               // Mã sản phẩm trong hệ thống
    private List<String> serialNumbers;      // Danh sách serial / IMEI của sản phẩm nhập về
}
