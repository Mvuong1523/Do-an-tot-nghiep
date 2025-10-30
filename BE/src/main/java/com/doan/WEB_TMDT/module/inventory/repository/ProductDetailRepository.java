package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.Product;
import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    Optional<ProductDetail> findBySerialNumber(String serialNumber);
    List<ProductDetail> findAllByProduct(Product product);
}
