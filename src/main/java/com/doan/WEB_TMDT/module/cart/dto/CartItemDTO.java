package com.doan.WEB_TMDT.module.cart.dto;

public class CartItemDTO {
    private Long productId;
    private int quantity;

    public CartItemDTO() {}

    public CartItemDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
