package com.doan.WEB_TMDT.module.inventory.service.impl;

import com.doan.WEB_TMDT.module.inventory.entity.ProductSpecification;
import com.doan.WEB_TMDT.module.inventory.entity.WarehouseProduct;
import com.doan.WEB_TMDT.module.inventory.repository.ProductSpecificationRepository;
import com.doan.WEB_TMDT.module.inventory.service.ProductSpecificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSpecificationServiceImpl implements ProductSpecificationService {

    private final ProductSpecificationRepository specRepository;

    @Override
    @Transactional
    public void parseAndSaveSpecs(WarehouseProduct product) {
        if (product.getTechSpecsJson() == null || product.getTechSpecsJson().trim().isEmpty()) {
            log.debug("No techSpecsJson to parse for product: {}", product.getSku());
            return;
        }

        try {
            // Xóa specs cũ
            specRepository.deleteByWarehouseProduct(product);

            // Parse JSON
            JSONObject json = new JSONObject(product.getTechSpecsJson());
            List<ProductSpecification> specs = new ArrayList<>();

            // Duyệt qua từng key-value
            json.keys().forEachRemaining(key -> {
                String value = json.optString(key);
                if (value != null && !value.trim().isEmpty()) {
                    ProductSpecification spec = ProductSpecification.builder()
                            .warehouseProduct(product)
                            .specKey(key)
                            .specValue(value)
                            .build();
                    specs.add(spec);
                }
            });

            // Lưu tất cả specs
            if (!specs.isEmpty()) {
                specRepository.saveAll(specs);
                log.info("Parsed and saved {} specifications for product: {}", specs.size(), product.getSku());
            }

        } catch (Exception e) {
            log.error("Error parsing techSpecsJson for product {}: {}", product.getSku(), e.getMessage());
            // Không throw exception để không ảnh hưởng việc lưu product
        }
    }

    @Override
    public List<WarehouseProduct> searchBySpecValue(String value) {
        return specRepository.findProductsBySpecValue(value);
    }

    @Override
    public List<WarehouseProduct> searchBySpecKeyAndValue(String key, String value) {
        return specRepository.findProductsBySpecKeyAndValue(key, value);
    }
}
