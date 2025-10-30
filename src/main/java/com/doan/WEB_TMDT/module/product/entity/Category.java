package com.project.ecommerce.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "category")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    private String description;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Tự tham chiếu: Danh mục cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    // Sản phẩm thuộc danh mục
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Product> products;
}