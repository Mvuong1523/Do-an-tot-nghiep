package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.inventory.entity.ProductStatus;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import com.doan.WEB_TMDT.module.product.entity.CatalogProduct;
import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    Optional<ProductDetail> findBySerialNumber(String serialNumber);
    List<ProductDetail> findByWarehouseProductAndStatus(WarehouseProduct product, ProductStatus status);
    long countByWarehouseProductAndStatus(WarehouseProduct product, ProductStatus status);

    boolean existsBySerialNumber(String serialNumber);
}
