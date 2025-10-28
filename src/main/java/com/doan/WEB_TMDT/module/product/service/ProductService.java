package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.common.dto.ApiResponse;

public interface ProductService {
    ApiResponse getProductQuantity(Long productId);
}
