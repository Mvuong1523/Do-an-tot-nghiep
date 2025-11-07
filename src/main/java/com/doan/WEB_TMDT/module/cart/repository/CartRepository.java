package com.project.ecommerce.cart.repository;

import com.project.ecommerce.auth.entity.User;
import com.project.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng theo đối tượng User
    Optional<Cart> findByUser(User user);
}