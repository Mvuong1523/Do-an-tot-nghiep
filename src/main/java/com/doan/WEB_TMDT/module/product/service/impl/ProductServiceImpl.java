package com.project.ecommerce.catalog.service.impl;

import com.project.ecommerce.catalog.dto.product.ProductCreateRequest;
import com.project.ecommerce.catalog.dto.product.VariantRequest;
import com.project.ecommerce.catalog.entity.Category;
import com.project.ecommerce.catalog.entity.Product;
import com.project.ecommerce.catalog.entity.Variant;
import com.project.ecommerce.catalog.repository.CategoryRepository;
import com.project.ecommerce.catalog.repository.ProductRepository;
import com.project.ecommerce.catalog.repository.VariantRepository;
import com.project.ecommerce.catalog.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private VariantRepository variantRepository;

    // CREATE: Thêm mới Sản phẩm (UC-09: 1.Tạo sản phẩm)
    @Override
    @Transactional
    public Product createProduct(ProductCreateRequest request) {
        // [RULE: Trùng Slug/SKU -> từ chối lưu] [cite: 36, 194]
        if (productRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new RuntimeException("Lỗi 1a: Slug sản phẩm '" + request.getSlug() + "' đã tồn tại.");
        }
        // [RULE: Phải nhập đầy đủ thông tin bắt buộc] [cite: 192]
        if (request.getVariants().isEmpty()) {
            throw new RuntimeException("Lỗi 5.1.1: Sản phẩm phải có ít nhất một biến thể.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại."));

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setCategory(category);
        product.setIsSelling(true);

        // Tạo Biến thể (Variant/SKU)
        Set<Variant> variants = request.getVariants().stream().map(vRequest -> {
            if (variantRepository.findBySku(vRequest.getSku()).isPresent()) {
                throw new RuntimeException("Lỗi 6.1.1: Mã SKU '" + vRequest.getSku() + "' đã tồn tại.");
            }

            Variant variant = new Variant();
            variant.setSku(vRequest.getSku());
            variant.setAttributes(vRequest.getAttributes());
            variant.setSellingPrice(vRequest.getSellingPrice());
            variant.setCurrentStock(vRequest.getInitialStock());
            variant.setProduct(product);
            return variant;
        }).collect(Collectors.toSet());

        product.setVariants(variants);

        return productRepository.save(product);
    }

    // UPDATE: Sửa Sản phẩm (UC-09: 1.Sửa sản phẩm)
    @Override
    @Transactional
    public Product updateProduct(Long id, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi 3.1.1: Sản phẩm không tồn tại."));

        // 1. Cập nhật thông tin chung
        product.setName(request.getName());
        product.setSlug(request.getSlug()); // Cần thêm logic kiểm tra trùng Slug nếu Slug thay đổi
        product.setDescription(request.getDescription());
        product.setListPrice(request.getListPrice());
        product.setUpdatedAt(LocalDateTime.now());

        Category newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại."));
        product.setCategory(newCategory);

        // 2. Xử lý Variants (Thêm/Sửa/Xóa)
        Set<Variant> updatedVariants = new HashSet<>();
        for (VariantRequest vRequest : request.getVariants()) {
            if (vRequest.getId() != null) {
                // Sửa Variant hiện có
                Variant variant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(vRequest.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Variant ID " + vRequest.getId() + " not found for this product."));

                // [RULE: Kiểm tra SKU trùng lặp khi sửa]
                if (variantRepository.findBySku(vRequest.getSku()).isPresent() && !vRequest.getSku().equals(variant.getSku())) {
                    throw new RuntimeException("Lỗi 6.1.1: Mã SKU '" + vRequest.getSku() + "' đã bị sử dụng bởi Variant khác.");
                }

                variant.setSku(vRequest.getSku());
                variant.setSellingPrice(vRequest.getSellingPrice());
                // Tồn kho (currentStock) chỉ nên được thay đổi qua nghiệp vụ kho (InventoryService),
                // chỉ update `initialStock` nếu đó là nghiệp vụ đặt hàng mới.
                updatedVariants.add(variant);
            } else {
                // Thêm Variant mới (Logic tương tự createProduct)
                if (variantRepository.findBySku(vRequest.getSku()).isPresent()) {
                    throw new RuntimeException("Lỗi 6.1.1: Mã SKU mới '" + vRequest.getSku() + "' đã tồn tại.");
                }
                Variant variant = new Variant();
                variant.setSku(vRequest.getSku());
                variant.setProduct(product);
                // ... set fields ...
                updatedVariants.add(variant);
            }
        }

        // Xóa các Variant cũ không còn trong danh sách mới (nhờ orphanRemoval=true)
        product.getVariants().clear();
        product.getVariants().addAll(updatedVariants);

        return productRepository.save(product);
    }

    // DELETE: Xóa Sản phẩm (UC-09: 1.Xóa sản phẩm)
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi 3.1.1: Sản phẩm không tồn tại."));

        // [RULE: Soft-delete B1: Áp dụng cho sản phẩm đã có giao dịch] [cite: 36, 196]
        // Vì sản phẩm công nghệ có giao dịch thường xuyên, ta ưu tiên Soft-delete.

        // Soft-delete: Đánh dấu không kinh doanh
        product.setIsSelling(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // Nếu muốn Hard-delete: Chỉ thực hiện khi sản phẩm chưa có bất kỳ giao dịch nào và không còn tồn kho.
        // productRepository.delete(product);
    }
}