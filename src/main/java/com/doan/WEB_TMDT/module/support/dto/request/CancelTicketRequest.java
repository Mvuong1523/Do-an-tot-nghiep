package com.doan.WEB_TMDT.module.support.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

// Huỷ phiếu
@Builder
@Data
public class CancelTicketRequest {
    @NotBlank
    private String cancelReason;
}
