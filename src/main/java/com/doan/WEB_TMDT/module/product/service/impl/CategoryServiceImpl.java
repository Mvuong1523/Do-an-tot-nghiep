package com.doan.WEB_TMDT.module.product.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.product.dto.CategoryDTO;
import com.doan.WEB_TMDT.module.product.dto.CreateCategoryRequest;
import com.doan.WEB_TMDT.module.product.entity.Category;
import com.doan.WEB_TMDT.module.product.repository.CategoryRepository;
import com.doan.WEB_TMDT.module.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

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

    @Override
    public ApiResponse getAllCategoriesTree() {
        List<Category> allCategories = categoryRepository.findAll();
        
        // Lấy các danh mục gốc (không có parent)
        List<CategoryDTO> rootCategories = allCategories.stream()
                .filter(cat -> cat.getParent() == null)
                .map(this::toCategoryDTO)
                .sorted((a, b) -> {
                    if (a.getDisplayOrder() == null) return 1;
                    if (b.getDisplayOrder() == null) return -1;
                    return a.getDisplayOrder().compareTo(b.getDisplayOrder());
                })
                .collect(Collectors.toList());
        
        return ApiResponse.success("Danh sách danh mục", rootCategories);
    }

    @Override
    public ApiResponse getActiveCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        
        List<CategoryDTO> activeCategories = allCategories.stream()
                .filter(cat -> cat.getActive() != null && cat.getActive())
                .filter(cat -> cat.getParent() == null) // Chỉ lấy danh mục gốc
                .map(this::toCategoryDTO)
                .sorted((a, b) -> {
                    if (a.getDisplayOrder() == null) return 1;
                    if (b.getDisplayOrder() == null) return -1;
                    return a.getDisplayOrder().compareTo(b.getDisplayOrder());
                })
                .collect(Collectors.toList());
        
        return ApiResponse.success("Danh mục đang hoạt động", activeCategories);
    }

    @Override
    public ApiResponse getCategoryWithProducts(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        CategoryDTO dto = toCategoryDTO(category);
        
        return ApiResponse.success("Chi tiết danh mục", dto);
    }

    @Override
    @Transactional
    public ApiResponse createCategory(CreateCategoryRequest request) {
        // Tạo slug từ name nếu không có
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }
        
        // Kiểm tra slug trùng
        if (categoryRepository.existsBySlug(slug)) {
            return ApiResponse.error("Slug đã tồn tại");
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        
        // Set parent nếu có
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }
        
        Category saved = categoryRepository.save(category);
        
        return ApiResponse.success("Tạo danh mục thành công", toCategoryDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.getActive());
        
        // Update slug nếu có
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            if (!request.getSlug().equals(category.getSlug())) {
                if (categoryRepository.existsBySlug(request.getSlug())) {
                    return ApiResponse.error("Slug đã tồn tại");
                }
                category.setSlug(request.getSlug());
            }
        }
        
        // Update parent
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                return ApiResponse.error("Danh mục không thể là cha của chính nó");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        Category updated = categoryRepository.save(category);
        
        return ApiResponse.success("Cập nhật danh mục thành công", toCategoryDTO(updated));
    }

    @Override
    public CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .productCount(category.getProductCount())
                .build();
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        // Lấy children
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryDTO> children = category.getChildren().stream()
                    .map(this::toCategoryDTO)
                    .sorted((a, b) -> {
                        if (a.getDisplayOrder() == null) return 1;
                        if (b.getDisplayOrder() == null) return -1;
                        return a.getDisplayOrder().compareTo(b.getDisplayOrder());
                    })
                    .collect(Collectors.toList());
            dto.setChildren(children);
        }
        
        return dto;
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
