package com.doan.WEB_TMDT.module.cart.service.impl;

import com.doan.WEB_TMDT.module.cart.dto.AddCartItemRequest;
import com.doan.WEB_TMDT.module.cart.dto.CartDTO;
import com.doan.WEB_TMDT.module.cart.dto.CartItemDTO;
import com.doan.WEB_TMDT.module.cart.dto.UpdateCartItemRequest;
import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import com.doan.WEB_TMDT.module.cart.entity.CartStatus;
import com.doan.WEB_TMDT.module.cart.repository.CartItemRepository;
import com.doan.WEB_TMDT.module.cart.repository.CartRepository;
import com.doan.WEB_TMDT.module.cart.service.CartService;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductShowStatus;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public CartDTO getCurrentCart(Long userId) {
        Cart cart = findOrCreateActiveCart(userId);
        return toDTO(cart);
    }

    @Override
    public CartDTO addItem(Long userId, AddCartItemRequest req) {
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }

        Cart cart = findOrCreateActiveCart(userId);
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        if (product.getStatus() != ProductShowStatus.ACTIVE) {
            throw new IllegalStateException("Sản phẩm hiện không được bán");
        }

        long stock = product.getStockQuantity() != null ? product.getStockQuantity() : Long.MAX_VALUE;
        int newQuantity = req.getQuantity();

        // tìm item cùng product trong giỏ
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (item != null) {
            newQuantity = item.getQuantity() + req.getQuantity();
        }

        if (newQuantity > stock) {
            throw new IllegalStateException("Số lượng yêu cầu vượt quá tồn kho");
        }

        if (item == null) {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .quantity(req.getQuantity())
                    .unitPrice(product.getSalePrice())
                    .lineTotal(product.getSalePrice() * req.getQuantity())
                    .build();
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
            item.setLineTotal(item.getUnitPrice() * newQuantity);
        }

        recalcCartTotals(cart);
        cart = cartRepository.save(cart);

        return toDTO(cart);
    }

    @Override
    public CartDTO updateItem(Long userId, Long cartItemId, UpdateCartItemRequest req) {
        if (req.getQuantity() == null || req.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        Cart cart = findOrCreateActiveCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy item trong giỏ"));

        if (req.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            long stock = product.getStockQuantity() != null ? product.getStockQuantity() : Long.MAX_VALUE;

            if (req.getQuantity() > stock) {
                throw new IllegalStateException("Số lượng yêu cầu vượt quá tồn kho");
            }

            item.setQuantity(req.getQuantity());
            item.setLineTotal(item.getUnitPrice() * req.getQuantity());
        }

        recalcCartTotals(cart);
        cart = cartRepository.save(cart);

        return toDTO(cart);
    }

    @Override
    public CartDTO removeItem(Long userId, Long cartItemId) {
        Cart cart = findOrCreateActiveCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy item trong giỏ"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        recalcCartTotals(cart);
        cart = cartRepository.save(cart);

        return toDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElse(null);
        if (cart == null) return;

        cart.getItems().clear();
        cart.setTotalAmount(0L);
        cart.setTotalItems(0);
        cartRepository.save(cart);
    }

    // ==================== HELPER ====================

    private Cart findOrCreateActiveCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart c = Cart.builder()
                            .userId(userId)
                            .status(CartStatus.ACTIVE)
                            .totalAmount(0L)
                            .totalItems(0)
                            .build();
                    return cartRepository.save(c);
                });
    }

    private void recalcCartTotals(Cart cart) {
        long totalAmount = 0L;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            totalAmount += item.getLineTotal();
            totalItems += item.getQuantity();
        }

        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
    }

    private CartDTO toDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(item -> {
                    Product p = item.getProduct();
                    Long stock = p.getStockQuantity();
                    return CartItemDTO.fromEntity(item, stock);
                })
                .toList();

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .items(itemDTOs)
                .build();
    }
}
