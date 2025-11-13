package com.doan.WEB_TMDT.module.cart.dto;

import java.util.List;

public class CartDTO {
    private Long customerId;
    private List<CartItemDTO> items;

    public CartDTO() {}

    public Long getCustomerId() {
        return customerId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }
}
