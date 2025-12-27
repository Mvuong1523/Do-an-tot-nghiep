package com.doan.WEB_TMDT.module.cart.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.repository.CustomerRepository;
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
    private final CustomerRepository customerRepository;
    private final com.doan.WEB_TMDT.module.product.repository.ProductImageRepository productImageRepository;

    @Override
    public Long getCustomerIdByEmail(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
        return customer.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getCart(Long customerId) {
        log.info("Getting cart for customerId: {}", customerId);
        Cart cart = getOrCreateCart(customerId);
        log.info("Cart found: id={}, items count={}", cart.getId(), cart.getItems().size());
        
        // Log each item
        cart.getItems().forEach(item -> {
            log.info("Cart item: id={}, product={}, quantity={}", 
                item.getId(), item.getProduct().getName(), item.getQuantity());
        });
        
        CartResponse response = toCartResponse(cart);
        log.info("CartResponse: items count={}", response.getItems().size());
        return ApiResponse.success("Giỏ hàng của bạn", response);
    }

    @Override
    @Transactional
    public ApiResponse addToCart(Long customerId, AddToCartRequest request) {
        // 1. Get or create cart
        Cart cart = getOrCreateCart(customerId);

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
    public ApiResponse updateCartItem(Long customerId, Long itemId, UpdateCartItemRequest request) {
        // 1. Get cart
        Cart cart = getOrCreateCart(customerId);

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
    public ApiResponse removeCartItem(Long customerId, Long itemId) {
        log.info("🗑️ Removing cart item - customerId: {}, itemId: {}", customerId, itemId);
        
        // 1. Get cart
        Cart cart = getOrCreateCart(customerId);
        log.info("📦 Found cart: id={}, items count={}", cart.getId(), cart.getItems().size());

        // 2. Find item
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));
        log.info("📦 Found item to delete: id={}, product={}", item.getId(), item.getProduct().getName());

        // 3. Verify ownership
        if (!item.getCart().getId().equals(cart.getId())) {
            log.error("❌ Ownership verification failed - item.cartId={}, user.cartId={}", 
                     item.getCart().getId(), cart.getId());
            return ApiResponse.error("Bạn không có quyền xóa sản phẩm này");
        }

        // 4. Remove item using Cart's removeItem method (important for JPA relationship)
        log.info("🗑️ Removing item from cart...");
        cart.removeItem(item);
        cartRepository.save(cart);  // Save to trigger orphanRemoval
        log.info("✅ Item removed successfully");

        // 5. Flush to ensure database is updated
        cartRepository.flush();

        // 6. Return updated cart
        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        log.info("📦 Updated cart: items count={}", updatedCart.getItems().size());
        
        CartResponse response = toCartResponse(updatedCart);
        log.info("✅ Returning response with {} items", response.getItems().size());
        return ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", response);
    }

    @Override
    @Transactional
    public ApiResponse clearCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        cartItemRepository.deleteByCartId(cart.getId());

        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        CartResponse response = toCartResponse(updatedCart);
        return ApiResponse.success("Đã xóa tất cả sản phẩm", response);
    }

    // Helper methods

    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
                    Cart newCart = Cart.builder()
                            .customer(customer)
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

        // Lấy ảnh đầu tiên (primary hoặc ảnh có displayOrder nhỏ nhất)
        String productImage = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElse(null);

        return CartItemResponse.builder()
                .itemId(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(productImage)
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
