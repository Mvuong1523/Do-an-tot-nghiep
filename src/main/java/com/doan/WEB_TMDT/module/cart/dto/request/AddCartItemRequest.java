package com.doan.WEB_TMDT.module.cart.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCartItemRequest {
    private Long productId;
    private int quantity;
}
