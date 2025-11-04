package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompletePORequest {
    private Long poId;                       // ID phiếu nhập cần hoàn tất
    private LocalDateTime receivedDate;      // Ngày nhập thực tế
    private List<ProductSerialRequest> serials; // Danh sách serial cho từng sản phẩm
}
