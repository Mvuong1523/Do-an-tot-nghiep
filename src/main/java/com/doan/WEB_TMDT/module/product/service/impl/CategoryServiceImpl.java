package com.project.ecommerce.catalog.service.impl;
// ... imports ...
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired private CategoryRepository categoryRepository;

    @Override @Transactional
    public Category createCategory(CategoryCreateRequest request) { /* Logic CREATE */ }

    @Override public List<Category> getAllCategories() { /* Logic READ */ }

    @Override @Transactional
    public Category updateCategory(Long id, CategoryCreateRequest request) { /* Logic UPDATE */ }

    @Override @Transactional
    public void deleteCategory(Long id) { /* Logic DELETE (Soft-delete) */ }
}