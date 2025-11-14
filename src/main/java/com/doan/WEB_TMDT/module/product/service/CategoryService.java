package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.product.dto.CategoryDTO;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryDTO create(CreateCategoryRequest req);

    CategoryDTO update(Long id, UpdateCategoryRequest req);

    void delete(Long id);

    CategoryDTO getById(Long id);

    List<CategoryDTO> getAll();
}
