package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, Long> {
    Optional<WarehouseProduct> findBySku(String sku);
}