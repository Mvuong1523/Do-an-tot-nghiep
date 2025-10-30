package com.doan.WEB_TMDT.module.auth.repository;

import com.doan.WEB_TMDT.module.auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Kiểm tra số điện thoại đã tồn tại chưa
    boolean existsByPhone(String phone);

    // Tìm khách hàng theo email (qua user.email)
    Optional<Customer> findByUserEmail(String email);

    // Tìm khách hàng theo số điện thoại
    Optional<Customer> findByPhone(String phone);
}
