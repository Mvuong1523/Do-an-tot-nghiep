package com.doan.WEB_TMDT.module.support.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho phản hồi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportReplyResponse {
    private Long id;
    private String senderType; // "customer" or "employee"
    private String senderName;
    private String senderEmail;
    private String content;
    private LocalDateTime createdAt;
}