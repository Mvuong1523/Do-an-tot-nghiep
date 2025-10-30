package com.project.ecommerce.catalog.service.impl;

import com.project.ecommerce.catalog.dto.category.CategoryCreateRequest;
import com.project.ecommerce.catalog.entity.Category;
import com.project.ecommerce.catalog.repository.CategoryRepository;
import com.project.ecommerce.catalog.repository.ProductRepository;
import com.project.ecommerce.catalog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    // READ: Lấy tất cả danh mục
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // CREATE: Thêm mới danh mục (UC-09: 1.Tạo danh mục)
    @Override
    @Transactional
    public Category createCategory(CategoryCreateRequest request) {
        // [RULE: Tên danh mục không được trùng với danh mục cùng danh mục cha] [cite: 186]
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Lỗi 5.2.1: Tên danh mục '" + request.getName() + "' đã tồn tại.");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);

        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại."));
            category.setParentCategory(parent);
        }

        return categoryRepository.save(category);
    }

    // UPDATE: Sửa danh mục (UC-09: 1.Sửa danh mục)
    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại."));

        // [RULE: Kiểm tra Tên trùng lặp (ngoại trừ chính nó)]
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Lỗi 5.3.1: Tên danh mục '" + request.getName() + "' đã trùng với danh mục khác.");
        }

        // Cập nhật trạng thái (Sửa đổi Soft-delete)
        if (request.getIsActive() != null && category.getIsActive() != request.getIsActive()) {
            if (!request.getIsActive()) {
                // Kiểm tra ràng buộc khi TẠM NGỪNG hoạt động
                if (category.getProducts().stream().anyMatch(Product::getIsSelling)) {
                    throw new RuntimeException("Không thể tạm ngừng. Danh mục này còn sản phẩm đang kinh doanh.");
                }
            }
            category.setIsActive(request.getIsActive());
        }

        // [RULE: Không được đổi sang danh mục cha đang tạm ngừng hoạt động] [cite: 188]
        if (request.getParentCategoryId() != null && !request.getParentCategoryId().equals(id)) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại."));

            if (!parent.getIsActive()) {
                throw new RuntimeException("Lỗi 5.2.1: Danh mục cha đang tạm ngừng, không thể đổi sang danh mục này.");
            }
            category.setParentCategory(parent);
        } else if (request.getParentCategoryId() == null) {
            category.setParentCategory(null);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryRepository.save(category);
    }

    // DELETE: Xóa danh mục (UC-09: 1.Xóa danh mục)
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại."));

        // [RULE: Soft-delete B1: Danh mục sẽ bị ẩn khỏi hệ thống] [cite: 190]
        // [RULE: Chỉ được xóa khi không có sản phẩm đang mở bán] [cite: 190]
        if (category.getProducts().stream().anyMatch(Product::getIsSelling)) {
            throw new RuntimeException("Lỗi 3.1.1: Danh mục này còn sản phẩm đang mở bán, cần gỡ hết trước khi xóa.");
        }

        // Soft-delete
        category.setIsActive(false);
        categoryRepository.save(category);
    }
}