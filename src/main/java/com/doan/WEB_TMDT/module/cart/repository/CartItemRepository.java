package com.doan.WEB_TMDT.module.cart.repository;

import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
