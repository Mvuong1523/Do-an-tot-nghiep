package com.doan.WEB_TMDT.module.delivery.repository;

import com.doan.WEB_TMDT.module.delivery.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    Optional<DeliveryOrder> findByOrderCode(String orderCode);
    Optional<DeliveryOrder> findByProviderLabel(String providerLabel);
}
