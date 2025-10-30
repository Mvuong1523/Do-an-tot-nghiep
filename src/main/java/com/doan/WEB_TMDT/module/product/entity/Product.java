package com.project.ecommerce.catalog.entity;
// ... imports ...
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "slug", unique = true)
    private String slug;
    @Lob private String description;
    @Column(name = "list_price", precision = 18, scale = 0)
    private BigDecimal listPrice;
    @Column(name = "is_selling", nullable = false)
    private Boolean isSelling = true;

    // Quan hệ Many-to-One với Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Quan hệ One-to-Many với Variant
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Variant> variants = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}