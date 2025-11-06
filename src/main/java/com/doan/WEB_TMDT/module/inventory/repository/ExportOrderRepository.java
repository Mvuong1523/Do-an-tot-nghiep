package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.ExportOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportOrderRepository extends JpaRepository<ExportOrder, Long> {
}
