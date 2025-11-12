package com.doan.WEB_TMDT.module.product.repository;

import com.doan.WEB_TMDT.module.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

    Optional<ProductDetail> findByProductId(Long productId);

    // (Từ lỗi trước) Kiểm tra sự tồn tại
    boolean existsBySerialNumber(String serialNumber);

    // 💡 PHƯƠNG THỨC CẦN THÊM ĐỂ SỬA LỖI LẦN NÀY (findBySerialNumber)
    Optional<ProductDetail> findBySerialNumber(String serialNumber);
}