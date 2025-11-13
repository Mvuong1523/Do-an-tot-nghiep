package com.doan.WEB_TMDT.module.cart.service;

import com.doan.WEB_TMDT.module.cart.dto.CartDTO;
import com.doan.WEB_TMDT.module.cart.entity.Cart;

public interface CartService {
    Cart getCartByCustomer(Long customerId);
    Cart addItemToCart(CartDTO cartDTO);
    Cart removeItemFromCart(Long customerId, Long productId);
    void clearCart(Long customerId);
}
