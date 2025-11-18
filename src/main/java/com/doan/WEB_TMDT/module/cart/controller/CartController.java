package com.doan.WEB_TMDT.module.cart.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.cart.dto.AddToCartRequest;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;
import com.doan.WEB_TMDT.module.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ApiResponse getCart(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return cartService.getCart(userId);
    }
    
    @PostMapping("/items")
    public ApiResponse addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        return cartService.addToCart(userId, request);
    }
    
    @PutMapping("/items/{itemId}")
    public ApiResponse updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        return cartService.updateCartItem(userId, itemId, request);
    }
    
    @DeleteMapping("/items/{itemId}")
    public ApiResponse removeCartItem(
            @PathVariable Long itemId,
            Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        return cartService.removeCartItem(userId, itemId);
    }
    
    @DeleteMapping
    public ApiResponse clearCart(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return cartService.clearCart(userId);
    }
    
    private Long getUserIdFromAuth(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }
        // Assuming principal is User object or email
        String email = auth.getName();
        // You might need to fetch user ID from UserRepository
        // For now, assuming you have a way to get it
        return 1L; // TODO: Implement proper user ID extraction
    }
}
