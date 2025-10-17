package com.doan.WEB_TMDT.entity.product;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories") // tên bảng tùy bạn
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Quan hệ ngược với Product
    @OneToMany(mappedBy = "category")
    private List<Product> products;

    // Getters và Setters
}
