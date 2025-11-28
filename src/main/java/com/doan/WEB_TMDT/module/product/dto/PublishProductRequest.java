package com.doan.WEB_TMDT.module.product.dto;

import lombok.Data;

@Data
public class PublishProductRequest {
    private Long warehouseProductId;  // ID của sản phẩm trong kho
    private Long categoryId;          // Danh mục hiển thị
    private String name;              // Tên hiển thị (có thể khác tên kho)
    private Double price;             // Giá bán
    private String description;       // Mô tả marketing
    private String imageUrl;          // Ảnh đại diện
}
