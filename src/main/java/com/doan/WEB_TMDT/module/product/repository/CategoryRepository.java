package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
