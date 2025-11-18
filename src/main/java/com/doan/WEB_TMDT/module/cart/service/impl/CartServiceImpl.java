package com.doan.WEB_TMDT.module.cart.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import com.doan.WEB_TMDT.module.cart.dto.*;
import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import com.doan.WEB_TMDT.module.cart.repository.CartItemRepository;
import com.doan.WEB_TMDT.module.cart.repository.CartRepository;
import com.doan.WEB_TMDT.module.cart.service.CartService;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Override
    public ApiResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        CartResponse response = mapToCartResponse(cart);
        return ApiResponse.success("Lấy giỏ hàng thành công", response);
    }
    
    @Override
    @Transactional
    public ApiResponse addToCart(Long userId, AddToCartRequest request) {
        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        // Check stock
        if (product.getStockQuantity() < request.getQuantity()) {
            return ApiResponse.error("Sản phẩm không đủ số lượng trong kho");
        }
        
        // Get or create cart
        Cart cart = getOrCreateCart(userId);
        
        // Check if product already in cart
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                return ApiResponse.error("Số lượng vượt quá tồn kho");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            cart.addItem(newItem);
            cartRepository.save(cart);
        }
        
        CartResponse response = mapToCartResponse(cart);
        return ApiResponse.success("Thêm vào giỏ hàng thành công", response);
    }
    
    @Override
    @Transactional
    public ApiResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));
        
        // Verify item belongs to user's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            return ApiResponse.error("Không có quyền cập nhật sản phẩm này");
        }
        
        // Check stock
        if (item.getProduct().getStockQuantity() < request.getQuantity()) {
            return ApiResponse.error("Số lượng vượt quá tồn kho");
        }
        
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        
        CartResponse response = mapToCartResponse(cart);
        return ApiResponse.success("Cập nhật giỏ hàng thành công", response);
    }
    
    @Override
    @Transactional
    public ApiResponse removeCartItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));
        
        // Verify item belongs to user's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            return ApiResponse.error("Không có quyền xóa sản phẩm này");
        }
        
        cart.removeItem(item);
        cartItemRepository.delete(item);
        
        CartResponse response = mapToCartResponse(cart);
        return ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công", response);
    }
    
    @Override
    @Transactional
    public ApiResponse clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        
        CartResponse response = mapToCartResponse(cart);
        return ApiResponse.success("Xóa toàn bộ giỏ hàng thành công", response);
    }
    
    // Helper methods
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());
        
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .build();
    }
    
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImageUrl())
                .sku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .stockQuantity(item.getProduct().getStockQuantity() != null 
                        ? item.getProduct().getStockQuantity().intValue() 
                        : 0)
                .build();
    }
}
