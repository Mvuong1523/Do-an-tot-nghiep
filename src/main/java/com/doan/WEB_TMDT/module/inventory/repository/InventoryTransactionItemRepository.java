package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.InventoryTransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionItemRepository extends JpaRepository<InventoryTransactionItem, Long> { }
