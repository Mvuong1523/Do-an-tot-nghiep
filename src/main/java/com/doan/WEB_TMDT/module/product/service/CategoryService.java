package com.doan.WEB_TMDT.module.product.service;

import com.doan.WEB_TMDT.module.product.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAll();
    Optional<Category> getById(Long id);
    Category create(Category category);
    Category update(Long id, Category category);
    void delete(Long id);
}
