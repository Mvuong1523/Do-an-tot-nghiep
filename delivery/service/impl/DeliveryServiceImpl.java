package com.doan.WEB_TMDT.module.delivery.service.impl;

import com.doan.WEB_TMDT.module.delivery.config.DeliveryProps;
import com.doan.WEB_TMDT.module.delivery.dto.*;
import com.doan.WEB_TMDT.module.delivery.entity.*;
import com.doan.WEB_TMDT.module.delivery.repository.DeliveryOrderRepository;
import com.doan.WEB_TMDT.module.delivery.service.DeliveryService;
import com.doan.WEB_TMDT.module.delivery.service.DeliveryStatusMapper;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException; import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryProps props;
    private final RestTemplate restTemplate;
    private final DeliveryOrderRepository deliveryOrderRepo;
    private final DeliveryStatusMapper statusMapper;

    // ===== Public API =====

    @Override
    public Object ping() {
        String url = props.getBaseUrl() + "/services/authenticated";
        return restTemplate.postForObject(url, new HttpEntity<>(new Object(), headers()), Object.class);
    }

    @Override
    public DeliveryCalculateFeeResponse calculateFee(DeliveryCalculateFeeRequest req) {
        String url = props.getBaseUrl() + "/services/shipment/fee/?ver=1.5";
        try {
            return restTemplate.postForEntity(url, new HttpEntity<>(req, headers()), DeliveryCalculateFeeResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Fee API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            var fail = new DeliveryCalculateFeeResponse();
            fail.setSuccess(false);
            fail.setMessage("Fee API error: " + e.getStatusCode());
            return fail;
        }
    }

    @Override
    public DeliveryCreateOrderResponse createOrderFromShop(ShopOrderRequest shopOrder) {
        var payload = toCarrierPayload(shopOrder);
        var res = callCreateOrder(payload);

        var delivery = deliveryOrderRepo.findByOrderCode(shopOrder.getOrderCode())
                .orElseGet(DeliveryOrder::new);
        delivery.setOrderCode(shopOrder.getOrderCode());
        delivery.setProvider("GHTK");

        if (res != null && res.isSuccess() && res.getOrder() != null) {
            var ord = res.getOrder();
            delivery.setProviderLabel(ord.getLabel());
            delivery.setProviderTrackingId(ord.getTracking_id());
            try { delivery.setShipFee(ord.getFee()!=null? Integer.parseInt(ord.getFee()):null);} catch (Exception ignore){}
            try { delivery.setInsuranceFee(ord.getInsurance_fee()!=null? Integer.parseInt(ord.getInsurance_fee()):null);} catch (Exception ignore){}
            delivery.setStatus(DeliveryStatus.SENT_TO_CARRIER);
        } else {
            delivery.setStatus(DeliveryStatus.REJECTED);
        }

        deliveryOrderRepo.save(delivery);
        return res;
    }

    @Override
    public void handleGhtkWebhook(WebhookGhtkPayload payload) {
        Optional<DeliveryOrder> opt =
                (payload.getLabel() != null)
                        ? deliveryOrderRepo.findByProviderLabel(payload.getLabel())
                        : Optional.empty();

        if (opt.isEmpty() && payload.getTracking_id() != null) {
            // fallback tìm theo tracking id (repo chưa có method; demo lọc tạm)
            opt = deliveryOrderRepo.findAll().stream()
                    .filter(d -> payload.getTracking_id().equals(d.getProviderTrackingId()))
                    .findFirst();
        }
        if (opt.isEmpty()) {
            log.warn("Webhook: không tìm thấy delivery_order cho label/tracking: {}", payload);
            return;
        }

        DeliveryOrder d = opt.get();
        d.setStatus(statusMapper.mapFromGhtk(payload.getStatus()));
        deliveryOrderRepo.save(d);
    }

    @Override
    public Optional<DeliveryOrder> findByOrderCode(String orderCode) {
        return deliveryOrderRepo.findByOrderCode(orderCode);
    }

    @Override
    public Optional<DeliveryOrder> findByProviderLabel(String label) {
        return deliveryOrderRepo.findByProviderLabel(label);
    }

    // ===== Internal helpers =====

    private DeliveryCreateOrderRequest toCarrierPayload(ShopOrderRequest s) {
        var req = new DeliveryCreateOrderRequest();
        var o = new DeliveryCreateOrderRequest.Order();

        // id đơn
        o.setId(s.getOrderCode());

        // pick (kho) từ config
        var p = props.getPickup();
        o.setPick_name(p.getName());
        o.setPick_address(p.getAddress());
        o.setPick_province(p.getProvince());
        o.setPick_district(p.getDistrict());
        o.setPick_ward(p.getWard());
        o.setPick_tel(p.getTel());

        // receiver
        o.setName(s.getCustomerName());
        o.setTel(s.getCustomerPhone());
        o.setAddress(s.getCustomerAddress());
        o.setProvince(s.getCustomerProvince());
        o.setDistrict(s.getCustomerDistrict());
        o.setWard(s.getCustomerWard());
        o.setHamlet("Khác");

        // COD/khai giá
        o.setPick_money(s.getCodAmount());
        o.setValue(s.getCodAmount());

        // option
        o.setIs_freeship(1);
        o.setTransport("fly");
        o.setPick_option("cod");

        // products
        List<DeliveryProduct> products = s.getItems().stream().map(it -> {
            var dp = new DeliveryProduct();
            dp.setName(it.getProductName());
            dp.setQuantity(it.getQuantity());
            dp.setWeight(it.getWeight() != null ? it.getWeight() : 0.1);
            dp.setPrice(it.getUnitPrice());
            return dp;
        }).toList();

        double totalWeight = products.stream().mapToDouble(DeliveryProduct::getWeight).sum();
        o.setTotal_weight(totalWeight);

        req.setOrder(o);
        req.setProducts(products);
        return req;
    }

    private DeliveryCreateOrderResponse callCreateOrder(DeliveryCreateOrderRequest payload) {
        String url = props.getBaseUrl() + "/services/shipment/order/?ver=1.5";
        try {
            return restTemplate.postForEntity(url, new HttpEntity<>(payload, headers()), DeliveryCreateOrderResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Create order API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            var fail = new DeliveryCreateOrderResponse();
            fail.setSuccess(false);
            fail.setMessage("Create order API error: " + e.getStatusCode());
            return fail;
        }
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Token", props.getToken());
        if (props.getPartnerCode() != null && !props.getPartnerCode().isBlank()) {
            h.set("X-Client-Source", props.getPartnerCode());
        }
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
