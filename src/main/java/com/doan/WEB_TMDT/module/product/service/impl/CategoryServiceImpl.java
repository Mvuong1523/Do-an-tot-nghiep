package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.module.product.dto.CategoryDTO;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.dto.UpdateCategoryRequest;
import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.repository.CategoryRepository;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDTO create(CreateCategoryRequest req) {
        if (categoryRepository.existsByName(req.getName())) {
            throw new IllegalArgumentException("Danh mục với tên này đã tồn tại");
        }

        Category c = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();

        c = categoryRepository.save(c);
        return CategoryDTO.fromEntity(c);
    }

    @Override
    public CategoryDTO update(Long id, UpdateCategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục"));

        if (req.getName() != null && !req.getName().isBlank()) {
            if (!c.getName().equals(req.getName())
                    && categoryRepository.existsByName(req.getName())) {
                throw new IllegalArgumentException("Danh mục với tên này đã tồn tại");
            }
            c.setName(req.getName());
        }

        if (req.getDescription() != null) {
            c.setDescription(req.getDescription());
        }

        c = categoryRepository.save(c);
        return CategoryDTO.fromEntity(c);
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy danh mục");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDTO getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục"));
        return CategoryDTO.fromEntity(c);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryDTO::fromEntity)
                .toList();
    }
}
