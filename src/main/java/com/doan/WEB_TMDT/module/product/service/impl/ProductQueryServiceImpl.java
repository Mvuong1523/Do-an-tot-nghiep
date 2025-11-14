package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.product.dto.ProductDTO;
import com.doan.WEB_TMDT.module.product.dto.ProductFilterRequest;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductShowStatus;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import com.doan.WEB_TMDT.module.product.service.ProductQueryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductDTO> searchPublic(ProductFilterRequest filter) {
        Specification<Product> spec = baseSpec(filter)
                .and((root, query, cb) ->
                        cb.equal(root.get("status"), ProductShowStatus.ACTIVE)
                );

        if (Boolean.TRUE.equals(filter.getOnlyInStock())) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThan(root.get("stockQuantity"), 0)
            );
        }

        Pageable pageable = buildPageable(filter);
        Page<Product> page = productRepository.findAll(spec, pageable);

        return page.map(ProductDTO::fromEntity);
    }

    @Override
    public Page<ProductDTO> searchAdmin(ProductFilterRequest filter) {
        Specification<Product> spec = baseSpec(filter);
        Pageable pageable = buildPageable(filter);
        Page<Product> page = productRepository.findAll(spec, pageable);

        return page.map(ProductDTO::fromEntity);
    }

    @Override
    public ProductDTO getDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Product với id = " + id));
        return ProductDTO.fromEntity(product);
    }

    private Specification<Product> baseSpec(ProductFilterRequest f) {
        Specification<Product> spec = Specification.where(null);

        if (f.getCategoryId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), f.getCategoryId())
            );
        }

        if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
            String like = "%" + f.getKeyword().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), like),
                            cb.like(cb.lower(root.get("brand")), like),
                            cb.like(cb.lower(root.get("model")), like)
                    )
            );
        }

        if (f.getMinPrice() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.ge(root.get("salePrice"), f.getMinPrice())
            );
        }
        if (f.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.le(root.get("salePrice"), f.getMaxPrice())
            );
        }

        return spec;
    }

    private Pageable buildPageable(ProductFilterRequest f) {
        Sort sort;
        String sortBy = f.getSortBy() == null ? "" : f.getSortBy();

        switch (sortBy) {
            case "priceAsc" -> sort = Sort.by("salePrice").ascending();
            case "priceDesc" -> sort = Sort.by("salePrice").descending();
            default -> sort = Sort.by("id").descending(); // newest
        }

        return PageRequest.of(f.getPage(), f.getSize(), sort);
    }
}
