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

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String slug; // URL-friendly name: dien-thoai, laptop-gaming

    private String description;
    
    private String imageUrl; // Ảnh đại diện cho danh mục
    
    private Integer displayOrder; // Thứ tự hiển thị
    
    private Boolean active; // Hiển thị hay ẩn
    
    // Phân cấp danh mục
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Category> children;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Product> products;
    
    // Helper method để lấy số lượng sản phẩm
    @Transient
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }
}
