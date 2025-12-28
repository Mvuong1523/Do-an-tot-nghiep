package com.doan.WEB_TMDT.module.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {
    
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
    
    @NotNull(message = "Order ID không được để trống")
    private Long orderId;
    
    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1-5")
    @Max(value = 5, message = "Rating phải từ 1-5")
    private Integer rating;
    
    private String comment; // Không bắt buộc
}
