package com.project.ecommerce.cart.service;

import com.project.ecommerce.cart.entity.Cart;
import com.project.ecommerce.cart.dto.CartItemRequest;
import com.project.ecommerce.cart.dto.CartResponse; // Sử dụng CartResponse cho đầu ra

public interface CartService {
    // READ
    Cart getOrCreateCartByUserId(Long userId);

    // CREATE/UPDATE
    Cart addItemToCart(Long userId, CartItemRequest request);

    // UPDATE
    Cart updateItemQuantity(Long userId, String sku, Integer quantity);

    // DELETE
    void removeItemFromCart(Long userId, String sku);
    void clearCart(Long userId);
}