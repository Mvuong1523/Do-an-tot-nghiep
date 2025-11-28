package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;

@Data
public class CompletePORequest {
    @NotNull(message = "poId không được null")
    private Long poId;                       // ID phiếu nhập cần hoàn tất
    private LocalDateTime receivedDate;      // Ngày nhập thực tế
    private List<ProductSerialRequest> serials; // Danh sách serial cho từng sản phẩm
}
