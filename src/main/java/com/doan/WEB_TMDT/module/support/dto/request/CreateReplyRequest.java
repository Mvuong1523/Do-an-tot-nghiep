package com.doan.WEB_TMDT.module.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Request DTO để gửi phản hồi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyRequest {

    @NotBlank(message = "Nội dung không được để trống")
    @Size(min = 1, max = 5000, message = "Nội dung không được quá 5000 ký tự")
    private String content;
}