package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.product.entity.Product1;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product1> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product1> getById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product1 create(Product1 product1) {
        return productRepository.save(product1);
    }

    @Override
    public Product1 update(Long id, Product1 product1) {
        if (productRepository.existsById(id)) {
            product1.setId(id);
            return productRepository.save(product1);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
