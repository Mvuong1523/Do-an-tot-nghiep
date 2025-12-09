package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // 💡 Cần import List để trả về danh sách

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Nó cho phép tìm tất cả Product dựa trên ID của nhà cung cấp liên kết qua WarehouseProduct.
    List<Product> findAllByWarehouseProduct_Supplier_Id(Long supplierId);
    
    // Đếm số sản phẩm theo category
    long countByCategory_Id(Long categoryId);
    
    // Tìm sản phẩm theo category
    List<Product> findByCategory_Id(Long categoryId);

}