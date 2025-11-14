package com.doan.WEB_TMDT.module.product.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên danh mục: Laptop, Điện thoại, Phụ kiện...
    @Column(nullable = false, unique = true)
    private String name;

    // Mô tả danh mục
    @Column(columnDefinition = "TEXT")
    private String description;

    // Sau này muốn thêm slug, active, parent thì chỉ cần bổ sung field ở đây

    // Quan hệ 1-n với Product
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
}
