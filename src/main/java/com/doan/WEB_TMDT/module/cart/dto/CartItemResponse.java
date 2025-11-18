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
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private String sku;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private Integer stockQuantity; // Số lượng còn trong kho
}
