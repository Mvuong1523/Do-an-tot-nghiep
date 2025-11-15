package com.doan.WEB_TMDT.module.cart.service.impl;

import com.doan.WEB_TMDT.module.cart.dto.CartItemRequest;
import com.project.ecommerce.cart.entity.Cart;
import com.project.ecommerce.cart.entity.CartItem;
import com.project.ecommerce.cart.repository.CartItemRepository;
import com.project.ecommerce.cart.repository.CartRepository;
import com.project.ecommerce.cart.service.CartService;
import com.project.ecommerce.catalog.entity.Variant;
import com.project.ecommerce.catalog.repository.VariantRepository;
import com.project.ecommerce.auth.entity.User;
import com.project.ecommerce.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private VariantRepository variantRepository; // Phụ thuộc vào Catalog
    @Autowired private UserRepository userRepository; // Phụ thuộc vào Auth

    private Cart getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: User không tồn tại."));

        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setItems(new HashSet<>());
            return cartRepository.save(newCart);
        });
    }

    @Override
    public Cart getOrCreateCartByUserId(Long userId) {
        return getCart(userId);
    }

    // CREATE/UPDATE: Thêm sản phẩm vào giỏ hàng (UC-04)
    @Override
    @Transactional
    public Cart addItemToCart(Long userId, CartItemRequest request) {
        Cart cart = getCart(userId);

        Variant variant = variantRepository.findBySku(request.getSku())
                .orElseThrow(() -> new RuntimeException("Lỗi 404: Không tìm thấy sản phẩm với SKU " + request.getSku()));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariant().getSku().equals(request.getSku()))
                .findFirst();

        int quantityToAdd = request.getQuantity();
        int currentCartQuantity = existingItem.map(CartItem::getQuantity).orElse(0);
        int finalQuantity = currentCartQuantity + quantityToAdd;

        // [LOGIC NGHIỆP VỤ: Kiểm tra TỒN KHO]
        if (variant.getCurrentStock() < finalQuantity) {
            throw new RuntimeException("Lỗi tồn kho: Chỉ còn " + variant.getCurrentStock() + " sản phẩm.");
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(finalQuantity);
            cartItemRepository.save(item);
        } else {
            // Thêm mới Item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setVariant(variant);
            newItem.setQuantity(quantityToAdd);
            newItem.setPriceSnapshot(variant.getSellingPrice()); // Snapshot giá
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTotalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum());
        return cartRepository.save(cart);
    }

    // UPDATE: Cập nhật số lượng
    @Override
    @Transactional
    public Cart updateItemQuantity(Long userId, String sku, Integer quantity) {
        if (quantity <= 0) {
            removeItemFromCart(userId, sku);
            return getCart(userId);
        }

        Cart cart = getCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getVariant().getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lỗi 404: Sản phẩm không có trong giỏ hàng."));

        Variant variant = item.getVariant();
        // [LOGIC NGHIỆP VỤ: Kiểm tra tồn kho cho số lượng mới]
        if (variant.getCurrentStock() < quantity) {
            throw new RuntimeException("Lỗi tồn kho: Số lượng cập nhật vượt quá giới hạn tồn kho.");
        }

        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTotalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum());
        return cartRepository.save(cart);
    }

    // DELETE: Xóa một món hàng
    @Override
    @Transactional
    public void removeItemFromCart(Long userId, String sku) {
        Cart cart = getCart(userId);
        Optional<CartItem> itemToRemove = cart.getItems().stream()
                .filter(i -> i.getVariant().getSku().equals(sku))
                .findFirst();

        if (itemToRemove.isPresent()) {
            cart.getItems().remove(itemToRemove.get());
            cart.setUpdatedAt(LocalDateTime.now());
            cart.setTotalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum());
            cartRepository.save(cart);
        }
    }

    // DELETE: Xóa toàn bộ giỏ
    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        cart.setTotalItems(0);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}