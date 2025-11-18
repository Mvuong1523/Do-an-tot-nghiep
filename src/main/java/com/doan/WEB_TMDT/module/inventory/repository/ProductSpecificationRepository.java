package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.ProductSpecification;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {

    // Xóa tất cả specs của 1 product (dùng khi update)
    void deleteByWarehouseProduct(WarehouseProduct product);

    // Search theo value (VD: "144Hz", "8GB")
    @Query("SELECT DISTINCT p.warehouseProduct FROM ProductSpecification p WHERE LOWER(p.specValue) LIKE LOWER(CONCAT('%', :value, '%'))")
    List<WarehouseProduct> findProductsBySpecValue(@Param("value") String value);

    // Search theo key + value (VD: refresh_rate = "144Hz")
    @Query("SELECT DISTINCT p.warehouseProduct FROM ProductSpecification p WHERE p.specKey = :key AND LOWER(p.specValue) LIKE LOWER(CONCAT('%', :value, '%'))")
    List<WarehouseProduct> findProductsBySpecKeyAndValue(@Param("key") String key, @Param("value") String value);

    // Lấy tất cả specs của 1 product
    List<ProductSpecification> findByWarehouseProduct(WarehouseProduct product);
}
