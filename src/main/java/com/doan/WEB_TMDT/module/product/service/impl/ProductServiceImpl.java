package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import com.doan.WEB_TMDT.module.product.entity.CatalogProduct;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.entity.CatalogProduct;
import com.doan.WEB_TMDT.module.inventory.entity.ProductStatus;

import com.doan.WEB_TMDT.module.product.repository.*;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final ProductSerialRepository serialRepo;

    @Override
    public ApiResponse getProductQuantity(Long productId) {
        //  Lấy thông tin sản phẩm hiển thị
        CatalogProduct product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));

        //  Lấy bản ghi sản phẩm thực trong kho
        WarehouseProduct warehouseProduct = product.getWarehouseProduct();
        if (warehouseProduct == null) {
            throw new IllegalStateException("Sản phẩm chưa liên kết với kho!");
        }

        //  Đếm serial theo trạng thái thực tế trong kho
        long available = serialRepo.countByWarehouseProductAndStatus(warehouseProduct, ProductStatus.IN_STOCK);
        long sold = serialRepo.countByWarehouseProductAndStatus(warehouseProduct, ProductStatus.SOLD);
        long returned = serialRepo.countByWarehouseProductAndStatus(warehouseProduct, ProductStatus.RETURNED);
        long damaged = serialRepo.countByWarehouseProductAndStatus(warehouseProduct, ProductStatus.DAMAGED);

        //  Trả về kết quả
        return ApiResponse.success("Số lượng sản phẩm trong kho", Map.of(
                "productId", product.getId(),
                "name", product.getDisplayName(),
                "available", available,  // ✅ hàng đang có thể bán
                "sold", sold,            // ✅ hàng đã bán
                "returned", returned,    // ✅ hàng khách trả
                "damaged", damaged       // ✅ hàng lỗi/hỏng
        ));
    }
}
