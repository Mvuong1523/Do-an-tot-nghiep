package com.doan.WEB_TMDT.module.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long customerId;
    private String shippingAddress;
    private String note;
    private List<OrderItemDTO> items;
}
