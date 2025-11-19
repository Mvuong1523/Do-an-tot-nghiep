package com.doan.WEB_TMDT.module.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long itemId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;
    private Integer quantity;
    private Double subtotal;
    private String serialNumber;
}
