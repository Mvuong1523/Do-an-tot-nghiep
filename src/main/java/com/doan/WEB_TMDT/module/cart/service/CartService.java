package com.doan.WEB_TMDT.module.cart.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.cart.dto.AddToCartRequest;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;

public interface CartService {
    ApiResponse getCart(Long userId);
    ApiResponse addToCart(Long userId, AddToCartRequest request);
    ApiResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request);
    ApiResponse removeCartItem(Long userId, Long itemId);
    ApiResponse clearCart(Long userId);
}
