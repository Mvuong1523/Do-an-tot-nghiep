package com.doan.WEB_TMDT.module.cart.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequest {
    private int quantity;
}
