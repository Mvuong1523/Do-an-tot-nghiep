package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class ProductSerialRequest {
    @NotBlank private String productSku;               // Mã sản phẩm trong hệ thống
    @NotEmpty private List<@NotBlank String> serialNumbers;      // Danh sách serial / IMEI của sản phẩm nhập về
}
