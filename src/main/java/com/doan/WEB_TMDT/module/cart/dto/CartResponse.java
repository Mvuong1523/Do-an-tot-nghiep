package com.project.ecommerce.cart.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    private Long cartId;
    private Long userId;
    private Integer totalItems;
    private BigDecimal subTotal;
    private BigDecimal estimatedShippingFee;
    private BigDecimal totalDiscount;
    private BigDecimal grandTotal;
    private List<CartItemResponse> items;
}

// Inner DTO cho chi tiết item
@Data
class CartItemResponse {
    private Long itemId;
    private String productName;
    private String productSku;
    private String attributes; // VD: Màu Đen, RAM 8GB
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}