package com.doan.WEB_TMDT.module.cart.controller;

import com.project.ecommerce.cart.service.CartService;
import com.project.ecommerce.cart.entity.Cart;
import com.project.ecommerce.cart.dto.CartItemRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Phương thức giả định lấy User ID từ cơ chế bảo mật
    private Long getCurrentUserId(UserDetails userDetails) {
        // Cần thay thế bằng logic lấy ID thực tế từ JWT/SecurityContext
        return 1L;
    }

    // [GET] Lấy thông tin Giỏ hàng hiện tại (READ)
    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        Cart cart = cartService.getOrCreateCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    // [POST] Thêm sản phẩm vào giỏ hàng (CREATE/UPDATE)
    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                        @Valid @RequestBody CartItemRequest request) {
        Long userId = getCurrentUserId(userDetails);
        Cart updatedCart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(updatedCart);
    }

    // [PUT] Cập nhật số lượng (UPDATE)
    @PutMapping("/items/{sku}")
    public ResponseEntity<Cart> updateItem(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable String sku,
                                           @RequestParam Integer quantity) {
        Long userId = getCurrentUserId(userDetails);
        Cart updatedCart = cartService.updateItemQuantity(userId, sku, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    // [DELETE] Xóa một món hàng (DELETE)
    @DeleteMapping("/items/{sku}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable String sku) {
        Long userId = getCurrentUserId(userDetails);
        cartService.removeItemFromCart(userId, sku);
        return ResponseEntity.noContent().build();
    }
}