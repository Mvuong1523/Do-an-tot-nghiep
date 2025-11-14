package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.product.dto.CreateProductFromWarehouseRequest;
import com.doan.WEB_TMDT.module.product.dto.UpdateProductRequest;
import com.doan.WEB_TMDT.module.product.entity.Product;

public interface ProductService {

    Product createFromWarehouse(CreateProductFromWarehouseRequest req);

    Product updatePrice(Long id, Long newPrice);

    Product updateBasicInfo(Long id, UpdateProductRequest req);

    void hide(Long id); // soft delete

    Product findByIdOrThrow(Long id);

    void syncStockFromWarehouse(Product product);
}
