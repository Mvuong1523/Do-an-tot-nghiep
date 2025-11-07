package com.project.ecommerce.cart.repository;

import com.project.ecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}