package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.product.dto.ProductDTO;
import com.doan.WEB_TMDT.module.product.dto.ProductFilterRequest;
import org.springframework.data.domain.Page;

public interface ProductQueryService {

    Page<ProductDTO> searchPublic(ProductFilterRequest filter);

    Page<ProductDTO> searchAdmin(ProductFilterRequest filter);

    ProductDTO getDetail(Long id);
}
