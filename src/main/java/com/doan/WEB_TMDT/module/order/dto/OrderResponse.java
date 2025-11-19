package com.doan.WEB_TMDT.module.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderCode;
    private String status;
    private String paymentStatus;
    
    // Customer info
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private String note;
    
    // Items
    private List<OrderItemResponse> items;
    
    // Pricing
    private Double subtotal;
    private Double shippingFee;
    private Double discount;
    private Double total;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
}
