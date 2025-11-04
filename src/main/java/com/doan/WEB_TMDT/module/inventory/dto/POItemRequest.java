package com.doan.WEB_TMDT.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class POItemRequest {
    @NotBlank
    private String sku;
    @NotNull
    @Positive
    private Long quantity;
    @NotNull @Positive private Double unitCost;
    @PositiveOrZero
    private Integer warrantyMonths;
    private String note;
}
