package com.doan.WEB_TMDT.module.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

// Cập nhật trạng thái
@Data@Builder
public class UpdateStatusRequest {
    @NotBlank
    private String newStatus; // CHO_XU_LY, DANG_XU_LY, DA_XU_LY, DA_HUY

    private String note;
}
