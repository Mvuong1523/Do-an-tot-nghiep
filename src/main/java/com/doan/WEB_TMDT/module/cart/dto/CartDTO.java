package com.doan.WEB_TMDT.module.cart.dto;

import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartDTO {

    private Long id;
    private Long userId;
    private Long totalAmount;
    private Integer totalItems;

    private List<CartItemDTO> items;
}
