package com.doan.WEB_TMDT.module.delivery.service;

import com.doan.WEB_TMDT.module.delivery.entity.DeliveryStatus;
import org.springframework.stereotype.Component;

/** Map mã trạng thái GHTK -> DeliveryStatus nội bộ */
@Component
public class DeliveryStatusMapper {
    public DeliveryStatus mapFromGhtk(Integer status) {
        if (status == null) return DeliveryStatus.CREATED;
        switch (status) {
            case 1: case 2: case 3:
                return DeliveryStatus.DELIVERING;
            case 4:
                return DeliveryStatus.DELIVERED;
            case 5:
                return DeliveryStatus.RETURNED;
            case 9:
                return DeliveryStatus.REJECTED;
            default:
                return DeliveryStatus.DELIVERING;
        }
    }
}
