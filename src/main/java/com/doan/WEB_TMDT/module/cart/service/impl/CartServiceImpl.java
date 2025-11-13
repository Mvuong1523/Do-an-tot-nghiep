package com.doan.WEB_TMDT.module.cart.service.impl;

import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.repository.CustomerRepository;
import com.doan.WEB_TMDT.module.cart.dto.CartDTO;
import com.doan.WEB_TMDT.module.cart.dto.CartItemDTO;
import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import com.doan.WEB_TMDT.module.cart.repository.CartRepository;
import com.doan.WEB_TMDT.module.cart.repository.CartItemRepository;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.cart.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Cart getCartByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Cart addItemToCart(CartDTO cartDTO) {
        Customer customer = customerRepository.findById(cartDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        for (CartItemDTO itemDTO : cartDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + itemDTO.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setCart(cart);
                newItem.setQuantity(itemDTO.getQuantity());
                cart.getItems().add(newItem);
            }
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItemFromCart(Long customerId, Long productId) {
        Cart cart = getCartByCustomer(customerId);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long customerId) {
        Cart cart = getCartByCustomer(customerId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
