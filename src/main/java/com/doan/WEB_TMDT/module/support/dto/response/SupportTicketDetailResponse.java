package com.doan.WEB_TMDT.module.support.dto.response;

import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.entity.Employee;
import com.doan.WEB_TMDT.module.order.dto.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho chi tiết phiếu (view đầy đủ)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String status;
    private String priority;
    private Long categoryId;
    private String categoryName;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long employeeId;
    private String employeeName;
    private List<OrderResponse> relatedOrders;
    private List<SupportReplyResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}