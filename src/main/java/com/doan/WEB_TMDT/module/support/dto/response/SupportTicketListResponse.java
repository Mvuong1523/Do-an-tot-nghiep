package com.doan.WEB_TMDT.module.support.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho danh sách phiếu (view tóm tắt)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketListResponse {
    private Long id;
    private String title;
    private String status;
    private String priority;
    private String categoryName;
    private String customerName;
    private String employeeName;
    private Boolean hasUnreadReply; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}