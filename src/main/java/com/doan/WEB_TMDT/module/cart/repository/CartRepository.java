package com.doan.WEB_TMDT.module.cart.repository;

import com.doan.WEB_TMDT.module.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.customer.id = :customerId")
    Optional<Cart> findByCustomerId(@Param("customerId") Long customerId);
    
    boolean existsByCustomerId(Long customerId);
}
