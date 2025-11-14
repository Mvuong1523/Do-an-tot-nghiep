package com.doan.WEB_TMDT.module.cart.controller;

import com.doan.WEB_TMDT.module.cart.dto.AddCartItemRequest;
import com.doan.WEB_TMDT.module.cart.dto.CartDTO;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;
import com.doan.WEB_TMDT.module.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDTO getMyCart() {
        Long userId = getCurrentUserId();
        return cartService.getCurrentCart(userId);
    }

    @PostMapping("/items")
    public CartDTO addItem(@RequestBody AddCartItemRequest req) {
        Long userId = getCurrentUserId();
        return cartService.addItem(userId, req);
    }

    @PatchMapping("/items/{itemId}")
    public CartDTO updateItem(@PathVariable Long itemId,
                              @RequestBody UpdateCartItemRequest req) {
        Long userId = getCurrentUserId();
        return cartService.updateItem(userId, itemId, req);
    }

    @DeleteMapping("/items/{itemId}")
    public CartDTO removeItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        return cartService.removeItem(userId, itemId);
    }

    @DeleteMapping
    public void clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
    }

    // TODO: thay bằng logic lấy userId thật từ SecurityContext/JWT
    private Long getCurrentUserId() {
        // ví dụ tạm, để code compile
        return 1L;
    }
}
