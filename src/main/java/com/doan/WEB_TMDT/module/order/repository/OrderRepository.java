package com.doan.WEB_TMDT.module.order.repository;

import com.doan.WEB_TMDT.module.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
}
