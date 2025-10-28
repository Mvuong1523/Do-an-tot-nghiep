package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.product.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    Optional<ProductSerial> findBySerial(String serial);
    List<ProductSerial> findByProductAndStatus(Product product, SerialStatus status);
    long countByProductAndStatus(Product product, SerialStatus status);
}
