package com.doan.WEB_TMDT.config;

import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // DISABLED: Tự động kích hoạt tất cả sản phẩm có active = null hoặc false
        // Chỉ chạy 1 lần khi khởi động lần đầu, sau đó comment lại
        
        /*
        try {
            var products = productRepository.findAll();
            int count = 0;
            
            for (Product product : products) {
                if (product.getActive() == null || !product.getActive()) {
                    product.setActive(true);
                    productRepository.save(product);
                    count++;
                    log.info("Auto-activated product: ID={}, name={}", product.getId(), product.getName());
                }
            }
            
            if (count > 0) {
                log.info("✅ Auto-activated {} products on startup", count);
            }
        } catch (Exception e) {
            log.error("❌ Error auto-activating products", e);
        }
        */
        
        log.info("DataInitializer: Auto-activation is DISABLED");
    }
}
