package com.doan.WEB_TMDT.module.cart.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.cart.dto.AddToCartRequest;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;

public interface CartService {
    Long getCustomerIdByEmail(String email);
    ApiResponse getCart(Long customerId);
    ApiResponse addToCart(Long customerId, AddToCartRequest request);
    ApiResponse updateCartItem(Long customerId, Long itemId, UpdateCartItemRequest request);
    ApiResponse removeCartItem(Long customerId, Long itemId);
    ApiResponse clearCart(Long customerId);
}
