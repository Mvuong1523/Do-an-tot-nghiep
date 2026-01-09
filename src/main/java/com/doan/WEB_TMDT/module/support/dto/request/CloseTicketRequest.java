package com.doan.WEB_TMDT.module.support.dto.request;

import lombok.Builder;
import lombok.Data;

// Đóng phiếu
@Data
@Builder
public class CloseTicketRequest {
    private String resolutionNote;
}
