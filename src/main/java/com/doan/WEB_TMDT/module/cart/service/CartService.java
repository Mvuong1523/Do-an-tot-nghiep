package com.doan.WEB_TMDT.module.cart.service;

import com.doan.WEB_TMDT.module.cart.dto.AddCartItemRequest;
import com.doan.WEB_TMDT.module.cart.dto.CartDTO;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;

public interface CartService {

    CartDTO getCurrentCart(Long userId);

    CartDTO addItem(Long userId, AddCartItemRequest req);

    CartDTO updateItem(Long userId, Long cartItemId, UpdateCartItemRequest req);

    CartDTO removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}
