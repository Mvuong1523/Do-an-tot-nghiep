package com.doan.WEB_TMDT.module.delivery.dto;

import lombok.Data;

/** Payload webhook từ GHTK (rút gọn – đủ trường hay dùng) */
@Data
public class WebhookGhtkPayload {
    private String label;          // nhãn đơn GHTK (S1.A1.xxxx)
    private Long tracking_id;      // id tracking
    private Integer status;        // mã trạng thái GHTK (int)
    private String status_text;    // mô tả trạng thái
    private String updated;        // thời điểm cập nhật
    private String reason;         // lý do (nếu fail)
    private String location;       // nơi cập nhật
    private Integer cod_amount;    // tiền COD (nếu có)
}
