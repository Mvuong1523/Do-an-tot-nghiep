package com.doan.WEB_TMDT.module.cart.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import com.doan.WEB_TMDT.module.cart.dto.AddToCartRequest;
import com.doan.WEB_TMDT.module.cart.dto.CartItemResponse;
import com.doan.WEB_TMDT.module.cart.dto.CartResponse;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;
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
import java.util.Optional;
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
        CartResponse response = toCartResponse(cart);
        return ApiResponse.success("Giỏ hàng của bạn", response);
    }

    @Override
    @Transactional
    public ApiResponse addToCart(Long userId, AddToCartRequest request) {
        // 1. Get or create cart
        Cart cart = getOrCreateCart(userId);

        // 2. Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // 3. Check stock
        if (product.getStockQuantity() == null || product.getStockQuantity() < request.getQuantity()) {
            return ApiResponse.error("Sản phẩm không đủ số lượng trong kho");
        }

        // 4. Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStockQuantity()) {
                return ApiResponse.error("Số lượng vượt quá tồn kho");
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            cartItemRepository.save(newItem);
        }

        // 5. Return updated cart
        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        CartResponse response = toCartResponse(updatedCart);
        return ApiResponse.success("Đã thêm vào giỏ hàng", response);
    }

    @Override
    @Transactional
    public ApiResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        // 1. Get cart
        Cart cart = getOrCreateCart(userId);

        // 2. Find item
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // 3. Verify ownership
        if (!item.getCart().getId().equals(cart.getId())) {
            return ApiResponse.error("Bạn không có quyền sửa sản phẩm này");
        }

        // 4. Check stock
        Product product = item.getProduct();
        if (product.getStockQuantity() == null || product.getStockQuantity() < request.getQuantity()) {
            return ApiResponse.error("Sản phẩm không đủ số lượng trong kho");
        }

        // 5. Update quantity
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        // 6. Return updated cart
        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        CartResponse response = toCartResponse(updatedCart);
        return ApiResponse.success("Đã cập nhật giỏ hàng", response);
    }

    @Override
    @Transactional
    public ApiResponse removeCartItem(Long userId, Long itemId) {
        // 1. Get cart
        Cart cart = getOrCreateCart(userId);

        // 2. Find item
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // 3. Verify ownership
        if (!item.getCart().getId().equals(cart.getId())) {
            return ApiResponse.error("Bạn không có quyền xóa sản phẩm này");
        }

        // 4. Remove item
        cartItemRepository.delete(item);

        // 5. Return updated cart
        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        CartResponse response = toCartResponse(updatedCart);
        return ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", response);
    }

    @Override
    @Transactional
    public ApiResponse clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());

        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        CartResponse response = toCartResponse(updatedCart);
        return ApiResponse.success("Đã xóa tất cả sản phẩm", response);
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

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        Double subtotal = cart.getSubtotal();
        Double shippingFee = calculateShippingFee(subtotal);
        Double discount = 0.0; // TODO: Calculate discount
        Double total = subtotal + shippingFee - discount;

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalItems(cart.getTotalItems())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discount(discount)
                .total(total)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        Product product = item.getProduct();
        boolean available = product.getStockQuantity() != null && 
                           product.getStockQuantity() >= item.getQuantity();

        return CartItemResponse.builder()
                .itemId(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getImageUrl())
                .productSku(product.getSku())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .stockQuantity(product.getStockQuantity() != null ? 
                              product.getStockQuantity().intValue() : 0)
                .subtotal(item.getSubtotal())
                .available(available)
                .build();
    }

    private Double calculateShippingFee(Double subtotal) {
        // TODO: Tính phí ship dựa trên địa chỉ
        // - Nội thành Hà Nội: Miễn phí (shipper riêng)
        // - Ngoại thành/Tỉnh: Gọi API GHTK
        // Tạm thời return 0, sẽ tính khi checkout (có địa chỉ cụ thể)
        return 0.0;
    }
}
