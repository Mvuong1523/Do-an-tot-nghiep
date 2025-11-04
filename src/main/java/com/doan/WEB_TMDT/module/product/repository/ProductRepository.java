package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.product.entity.CatalogProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<CatalogProduct, Long> {
    boolean existsBySku(String sku);
    Optional<CatalogProduct> findById(Long aLong);
    Optional<CatalogProduct> findBySku(String sku);
    List<CatalogProduct> findAllBySupplier_Id(Long supplierId);

}