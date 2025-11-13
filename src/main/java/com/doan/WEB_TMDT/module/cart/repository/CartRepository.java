package com.doan.WEB_TMDT.module.cart.repository;

import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
}
