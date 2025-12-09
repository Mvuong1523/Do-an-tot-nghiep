package com.doan.WEB_TMDT.module.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductFromWarehouseRequest {
    
    @NotNull(message = "Warehouse Product ID không được để trống")
    private Long warehouseProductId;
    
    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;
    
    @NotNull(message = "Tên sản phẩm không được để trống")
    private String name;
    
    @NotNull(message = "Giá bán không được để trống")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private Double price;
    
    private String description;
}
