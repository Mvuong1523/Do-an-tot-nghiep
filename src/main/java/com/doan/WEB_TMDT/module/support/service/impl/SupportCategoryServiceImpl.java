package com.doan.WEB_TMDT.module.support.service.impl;

import com.doan.WEB_TMDT.module.support.dto.response.SupportCategoryResponse;
import com.doan.WEB_TMDT.module.support.entities.SupportCategory;
import com.doan.WEB_TMDT.module.support.exceptions.NotFoundException;
import com.doan.WEB_TMDT.module.support.repository.SupportCategoryRepository;
import com.doan.WEB_TMDT.module.support.service.SupportCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private SupportCategoryResponse mapToResponse(SupportCategory category) {
        return SupportCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}