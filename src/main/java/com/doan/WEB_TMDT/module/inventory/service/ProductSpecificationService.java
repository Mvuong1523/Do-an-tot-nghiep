package com.doan.WEB_TMDT.module.inventory.service;

import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;

import java.util.List;

public interface ProductSpecificationService {
    
    /**
     * Parse techSpecsJson và lưu vào bảng product_specifications
     */
    void parseAndSaveSpecs(WarehouseProduct product);
    
    /**
     * Search sản phẩm theo thông số (VD: "144Hz", "8GB")
     */
    List<WarehouseProduct> searchBySpecValue(String value);
    
    /**
     * Search sản phẩm theo key + value (VD: ram = "8GB")
     */
    List<WarehouseProduct> searchBySpecKeyAndValue(String key, String value);
}
