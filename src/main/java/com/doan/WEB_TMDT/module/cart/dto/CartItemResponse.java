package com.doan.WEB_TMDT.module.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long itemId;
    private Long productId;
    private String productName;
    private String productImage;
    private String productSku;
    private Double price;
    private Integer quantity;
    private Integer stockQuantity;
    private Double subtotal;
    private Boolean available; // Còn hàng không
}
