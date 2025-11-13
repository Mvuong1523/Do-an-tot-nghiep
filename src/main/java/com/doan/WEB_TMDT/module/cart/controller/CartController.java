package com.doan.WEB_TMDT.module.cart.controller;

import com.doan.WEB_TMDT.module.cart.dto.CartDTO;
import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.cart.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public Cart getCart(@PathVariable Long customerId) {
        return cartService.getCartByCustomer(customerId);
    }

    @PostMapping("/add")
    public Cart addToCart(@RequestBody CartDTO cartDTO) {
        return cartService.addItemToCart(cartDTO);
    }

    @DeleteMapping("/{customerId}/remove/{productId}")
    public Cart removeItem(@PathVariable Long customerId, @PathVariable Long productId) {
        return cartService.removeItemFromCart(customerId, productId);
    }

    @DeleteMapping("/{customerId}/clear")
    public void clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
    }
}
