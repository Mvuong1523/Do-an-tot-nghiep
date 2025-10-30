package com.project.ecommerce.catalog.service;
// ... imports ...
public interface CategoryService {
    Category createCategory(CategoryCreateRequest request); // CREATE
    List<Category> getAllCategories(); // READ
    Category updateCategory(Long id, CategoryCreateRequest request); // UPDATE
    void deleteCategory(Long id); // DELETE (Soft-delete)
}