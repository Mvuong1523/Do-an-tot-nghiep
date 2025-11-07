package com.doan.WEB_TMDT.module.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExportInventoryRequest {
    private String reason;                  // Lý do xuất (bán hàng / chuyển kho / hủy hàng)
    private Long purchaseOrderId;// mã phiếu nhập (nếu có)
    private  String createdBy;
    private List<ExportItemRequest> items;  // Danh sách sản phẩm xuất
    private  String note;
}



