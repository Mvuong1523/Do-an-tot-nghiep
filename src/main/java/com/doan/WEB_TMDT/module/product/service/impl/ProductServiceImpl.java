package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductStatus;

import com.doan.WEB_TMDT.module.product.repository.*;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final ProductSerialRepository serialRepo;

    @Override
    public ApiResponse getProductQuantity(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));

        long available = serialRepo.countByProductAndStatus(product, ProductStatus.IN_STOCK);
        long sold = serialRepo.countByProductAndStatus(product, ProductStatus.SOLD);
        long returned = serialRepo.countByProductAndStatus(product, ProductStatus.RETURNED);

        return ApiResponse.success("Số lượng sản phẩm hiện tại",
                java.util.Map.of(
                        "productId", product.getId(),
                        "sku", product.getSku(),
                        "name", product.getName(),
                        "available", available,
                        "sold", sold,
                        "returned", returned
                ));
    }
}
