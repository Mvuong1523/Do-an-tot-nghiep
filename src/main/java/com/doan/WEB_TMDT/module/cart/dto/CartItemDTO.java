package com.doan.WEB_TMDT.module.cart.dto;

import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemDTO {

    private Long id;

    private Long productId;
    private String productName;
    private String productImage;

    private Integer quantity;
    private Long unitPrice;
    private Long lineTotal;

    // để FE cảnh báo nếu thiếu hàng
    private Long stockAvailable;
    private boolean canPurchase;

    public static CartItemDTO fromEntity(CartItem item, Long stockAvailable) {
        boolean canPurchase = stockAvailable == null || stockAvailable >= item.getQuantity();

        return CartItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .stockAvailable(stockAvailable)
                .canPurchase(canPurchase)
                .build();
    }
}
