package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentIsNull(); // Lấy danh mục gốc
    List<Category> findByParentId(Long parentId); // Lấy danh mục con
    List<Category> findByActiveTrue(); // Lấy danh mục đang active
}
