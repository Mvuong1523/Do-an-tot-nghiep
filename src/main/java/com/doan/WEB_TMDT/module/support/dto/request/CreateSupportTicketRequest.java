package com.doan.WEB_TMDT.module.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo phiếu hỗ trợ mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSupportTicketRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10-200 ký tự")
    private String title;

    @NotNull(message = "Loại yêu cầu không được để trống")
    private Long supportCategoryId;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(min = 20, max = 5000, message = "Nội dung phải từ 20-5000 ký tự")
    private String content;

    private Long orderId;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Mức độ ưu tiên không hợp lệ")
    private String priority = "MEDIUM";
}
