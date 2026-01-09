package com.doan.WEB_TMDT.module.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật trạng thái phiếu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTicketStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "PENDING|PROCESSING|RESOLVED|CANCELLED",
            message = "Trạng thái không hợp lệ")
    private String newStatus;

    private String note; // Ghi chú khi thay đổi trạng thái
}