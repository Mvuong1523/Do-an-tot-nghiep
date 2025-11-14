package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.repository.CategoryRepository;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category category) {
        if (categoryRepository.existsById(id)) {
            category.setId(id);
            return categoryRepository.save(category);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
