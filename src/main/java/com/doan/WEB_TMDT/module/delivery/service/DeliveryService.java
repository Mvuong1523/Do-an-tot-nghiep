package com.doan.WEB_TMDT.module.delivery.service;

import com.doan.WEB_TMDT.module.delivery.dto.*;
import com.doan.WEB_TMDT.module.delivery.entity.DeliveryOrder;

import java.util.Optional;

public interface DeliveryService {

    Object ping();

    DeliveryCalculateFeeResponse calculateFee(DeliveryCalculateFeeRequest req);

    DeliveryCreateOrderResponse createOrderFromShop(ShopOrderRequest shopOrder);

    void handleGhtkWebhook(WebhookGhtkPayload payload);

    Optional<DeliveryOrder> findByOrderCode(String orderCode);

    Optional<DeliveryOrder> findByProviderLabel(String label);
}
