package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import com.doan.WEB_TMDT.module.inventory.entity.ProductStatus;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductSerialRepository extends JpaRepository<ProductDetail, Long> {
    Optional<ProductDetail> findBySerialNumber(String serialNumber);
    List<ProductDetail> findByWarehouseProductAndStatus(WarehouseProduct product, ProductStatus status);
    long countByWarehouseProductAndStatus(WarehouseProduct product, ProductStatus status);
}
