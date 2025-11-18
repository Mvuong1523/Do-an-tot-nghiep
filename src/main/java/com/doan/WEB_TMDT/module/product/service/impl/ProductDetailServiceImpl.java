package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import com.doan.WEB_TMDT.module.inventory.repository.ProductDetailRepository;
import com.doan.WEB_TMDT.module.product.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductDetailRepository productDetailRepository;

    @Override
    public List<ProductDetail> getAll() {
        return productDetailRepository.findAll();
    }

    @Override
    public Optional<ProductDetail> getById(Long id) {
        return productDetailRepository.findById(id);
    }

    @Override
    public ProductDetail create(ProductDetail productDetail) {
        return productDetailRepository.save(productDetail);
    }

    @Override
    public ProductDetail update(Long id, ProductDetail productDetail) {
        if (productDetailRepository.existsById(id)) {
            productDetail.setId(id);
            return productDetailRepository.save(productDetail);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        productDetailRepository.deleteById(id);
    }
}
