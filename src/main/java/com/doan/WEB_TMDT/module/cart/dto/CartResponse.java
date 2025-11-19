package com.doan.WEB_TMDT.module.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Double subtotal;
    private Double shippingFee;
    private Double discount;
    private Double total;
}
