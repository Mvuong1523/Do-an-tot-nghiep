package com.doan.WEB_TMDT.module.support.repository;

import com.doan.WEB_TMDT.module.support.entities.SupportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportCategoryRepository extends JpaRepository<SupportCategory, Long> {
    List<SupportCategory> findByIsActiveTrue();

}
