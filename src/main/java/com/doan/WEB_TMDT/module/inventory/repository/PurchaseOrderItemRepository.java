package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
}
