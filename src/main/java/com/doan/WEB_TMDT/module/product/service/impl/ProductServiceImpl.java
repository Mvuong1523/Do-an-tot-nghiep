package com.project.ecommerce.catalog.service.impl;
// ... imports ...
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Override @Transactional
    public Product createProduct(ProductCreateRequest request) { /* Logic CREATE */ }

    @Override @Transactional
    public Product updateProduct(Long id, ProductCreateRequest request) { /* Logic UPDATE phức tạp */ }

    @Override @Transactional
    public void deleteProduct(Long id) { /* Logic DELETE (Soft-delete) */ }
}