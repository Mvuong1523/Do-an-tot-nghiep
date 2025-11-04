package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.InventoryStock;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByProductId(Long Id);
    Optional<WarehouseProduct> findBySku(String sku);
    List<WarehouseProduct> findAllBySupplier_Id(Long supplierId);
    boolean existsBySku(String sku);
    Optional<InventoryStock> findByWarehouseProduct_Id(Long id);
}
