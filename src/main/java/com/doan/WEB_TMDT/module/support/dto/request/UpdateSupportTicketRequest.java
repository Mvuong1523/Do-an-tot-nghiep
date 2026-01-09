package com.doan.WEB_TMDT.module.support.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật thông tin phiếu (Employee)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupportTicketRequest {

    private Long supportCategoryId;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Mức độ ưu tiên không hợp lệ")
    private String priority;

    private String internalNote; // Ghi chú nội bộ
}
