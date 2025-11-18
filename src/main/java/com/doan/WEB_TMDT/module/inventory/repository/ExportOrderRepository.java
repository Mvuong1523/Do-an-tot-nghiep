package com.doan.WEB_TMDT.module.inventory.repository;

import com.doan.WEB_TMDT.module.inventory.entity.ExportOrder;
import com.doan.WEB_TMDT.module.inventory.entity.ExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportOrderRepository extends JpaRepository<ExportOrder, Long> {
    List<ExportOrder> findByStatus(ExportStatus status);
}
