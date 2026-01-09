package com.doan.WEB_TMDT.module.support.service;

import com.doan.WEB_TMDT.module.support.dto.request.SupportCategoryRequest;
import com.doan.WEB_TMDT.module.support.dto.response.SupportCategoryResponse;

import java.util.List;

/**
 * Interface cho Service quản lý Category
 */
public interface SupportCategoryService {

    /**
     * Lấy danh sách category đang active
     * @return Danh sách category
     */
    List<SupportCategoryResponse> getActiveCategories();

    /**
     * Lấy tất cả category (bao gồm inactive) - Admin
     * @return Danh sách tất cả category
     */
    List<SupportCategoryResponse> getAllCategories();

    /**
     * Lấy chi tiết category
     * @param id ID của category
     * @return Chi tiết category
     */
    SupportCategoryResponse getCategoryById(Long id);


    void createCategory(SupportCategoryRequest request);
    boolean updateCategory(Long id, SupportCategoryRequest request);
    boolean deleteCategory(Long id);
    boolean toggleActive(Long id);

}
