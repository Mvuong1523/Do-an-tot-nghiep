package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.support.dto.request.SupportCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportCategoryResponse;
import com.doan.WEB_TMDT.module.support.entities.SupportCategory;
import com.doan.WEB_TMDT.module.support.exceptions.NotFoundException;
import com.doan.WEB_TMDT.module.support.repository.SupportCategoryRepository;
import com.doan.WEB_TMDT.module.support.service.SupportCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportCategoryServiceImpl implements SupportCategoryService {

    private final SupportCategoryRepository categoryRepository;

    @Override
    public List<SupportCategoryResponse> getActiveCategories() {
        log.info("Getting active categories");

        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupportCategoryResponse> getAllCategories() {
        log.info("Getting all categories");

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupportCategoryResponse getCategoryById(Long id) {
        log.info("Getting category by id: {}", id);

        SupportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = false)
    public void createCategory(SupportCategoryRequest request) {
        log.info("Create a new category: {}", request);
        SupportCategory supportCategory = SupportCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .createdAt(LocalDateTime.now())
                .build();
        this.categoryRepository.save(supportCategory);

    }

    @Override
    public boolean updateCategory(Long id, SupportCategoryRequest request) {
        log.info("Update a category: {}", request);
        SupportCategory supportCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        supportCategory.setName(request.getName());
        supportCategory.setDescription(request.getDescription());
        supportCategory.setIsActive(request.getIsActive());
        try {
            this.categoryRepository.save(supportCategory);
            return true;
        }catch (Exception e){
            return false;
        }

    }

    @Override
    public boolean deleteCategory(Long id) {
        log.info("Delete a category: {}", id);
        try {
            this.categoryRepository.deleteById(id);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean toggleActive(Long id) {
        log.info("Toggle active category: {}", id);
        SupportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        category.setIsActive(!category.getIsActive());
        try {
            this.categoryRepository.save(category);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private SupportCategoryResponse mapToResponse(SupportCategory category) {
        return SupportCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}