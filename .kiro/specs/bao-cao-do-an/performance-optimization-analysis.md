# Phân Tích Performance Optimization

## Tổng Quan

Tài liệu này phân tích các chiến lược tối ưu hóa hiệu suất cho hệ thống TMDT, bao gồm tối ưu hóa truy vấn database, chiến lược indexing, caching, lazy/eager loading, pagination và connection pooling.

## 1. Database Query Optimization

### 1.1. Phân Tích Các Truy Vấn Hiện Tại

#### Truy Vấn Phức Tạp Cần Tối Ưu

**OrderRepository - findByStatusAndNotExported**
```java
@Query("SELECT o FROM Order o WHERE o.status = :status " +
       "AND NOT EXISTS (SELECT 1 FROM ExportOrder e WHERE e.orderId = o.id) " +
       "ORDER BY o.confirmedAt DESC")
List<Order> findByStatusAndNotExported(@Param("status") OrderStatus status);
```

**Vấn đề**: 
- Subquery NOT EXISTS có thể chậm với dữ liệu lớn
- Không có index trên `orderId` của ExportOrder

**Giải pháp**:
```java
// Tối ưu với LEFT JOIN
@Query("SELECT o FROM Order o " +
       "LEFT JOIN ExportOrder e ON e.orderId = o.id " +
       "WHERE o.status = :status AND e.id IS NULL " +
       "ORDER BY o.confirmedAt DESC")
List<Order> findByStatusAndNotExported(@Param("status") OrderStatus status);
```

**OrderRepository - Aggregate Queries**
```java
@Query("SELECT SUM(o.total) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
       "AND o.paymentStatus = com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PAID")
Double sumTotalByDateRange(@Param("startDate") LocalDateTime startDate, 
                           @Param("endDate") LocalDateTime endDate);
```

**Vấn đề**:
- Scan toàn bộ bảng nếu không có index trên `createdAt` và `paymentStatus`
- Aggregate function trên dữ liệu lớn có thể chậm

**Giải pháp**:
- Thêm composite index: `(createdAt, paymentStatus, total)`
- Sử dụng materialized view hoặc summary table cho báo cáo

### 1.2. N+1 Query Problem

**Vấn đề Phát Hiện trong ProductServiceImpl**
```java
@Override
public List<Product> getAll() {
    List<Product> products = productRepository.findAll();
    // Eager load category để tránh lazy loading exception
    products.forEach(product -> {
        if (product.getCategory() != null) {
            product.getCategory().getName(); // Trigger lazy load - N+1 PROBLEM!
        }
    });
    return products;
}
```

**Giải pháp 1: Sử dụng JOIN FETCH**
```java
@Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
List<Product> findAllWithCategory();
```

**Giải pháp 2: Entity Graph**
```java
@EntityGraph(attributePaths = {"category", "images"})
List<Product> findAll();
```

**Giải pháp 3: DTO Projection**
```java
@Query("SELECT new com.doan.WEB_TMDT.module.product.dto.ProductDTO(" +
       "p.id, p.name, p.price, c.name) " +
       "FROM Product p LEFT JOIN p.category c")
List<ProductDTO> findAllAsDTO();
```

### 1.3. Batch Operations

**Vấn đề**: Import nhiều sản phẩm từ Excel
```java
// Hiện tại: Save từng item một
for (ProductItem item : items) {
    productRepository.save(item); // N queries!
}
```

**Giải pháp**: Batch Insert
```java
// application.properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

// Code
@Transactional
public void importProducts(List<Product> products) {
    int batchSize = 50;
    for (int i = 0; i < products.size(); i++) {
        productRepository.save(products.get(i));
        if (i % batchSize == 0 && i > 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

### 1.4. Query Hints và Optimization

**Read-Only Queries**
```java
@Transactional(readOnly = true)
@QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
List<Product> findAllForDisplay();
```

**Fetch Size Optimization**
```java
@QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
List<Order> findLargeResultSet();
```

## 2. Indexing Strategy

### 2.1. Phân Tích Index Hiện Tại

**Các Index Đã Có**:
- Primary keys (tự động)
- Unique constraints: `orderCode`, `ghnOrderCode`, `sku`
- ProductSpecification: `idx_spec_key` trên `spec_key`
- InventoryStock: unique constraint trên `product_id`

**Thiếu Index Quan Trọng**:
- Không có index trên foreign keys
- Không có index trên các trường thường xuyên query (status, createdAt)
- Không có composite index cho các query phức tạp

### 2.2. Đề Xuất Index Mới

#### Order Table
```sql
-- Index cho status queries
CREATE INDEX idx_orders_status ON orders(status);

-- Index cho customer orders
CREATE INDEX idx_orders_customer_id ON orders(customer_id);

-- Composite index cho date range queries
CREATE INDEX idx_orders_created_payment ON orders(created_at, payment_status);

-- Index cho GHN tracking
CREATE INDEX idx_orders_ghn_code ON orders(ghn_order_code);

-- Composite index cho warehouse queries
CREATE INDEX idx_orders_status_confirmed ON orders(status, confirmed_at DESC);
```

#### FinancialTransaction Table
```sql
-- Index cho date range queries
CREATE INDEX idx_financial_date ON financial_transaction(transaction_date);

-- Composite index cho category reports
CREATE INDEX idx_financial_type_category_date ON financial_transaction(type, category, transaction_date);

-- Index cho order lookup
CREATE INDEX idx_financial_order_id ON financial_transaction(order_id);
```

#### InventoryStock Table
```sql
-- Index cho warehouse product lookup
CREATE INDEX idx_inventory_warehouse_product ON inventory_stock(warehouse_product_id);

-- Index cho low stock alerts
CREATE INDEX idx_inventory_onhand ON inventory_stock(on_hand);
```

#### ExportOrder Table
```sql
-- Index cho order lookup (fix N+1 problem)
CREATE INDEX idx_export_order_id ON export_orders(order_id);

-- Index cho status queries
CREATE INDEX idx_export_status ON export_orders(status);

-- Composite index cho date queries
CREATE INDEX idx_export_date_status ON export_orders(export_date, status);
```

#### Payment Table
```sql
-- Index cho order lookup
CREATE INDEX idx_payment_order_id ON payments(order_id);

-- Index cho status queries
CREATE INDEX idx_payment_status ON payments(status);

-- Index cho expired payment cleanup
CREATE INDEX idx_payment_expired ON payments(status, expired_at);
```

#### ProductImage Table
```sql
-- Index cho product images lookup
CREATE INDEX idx_product_image_product_id ON product_images(product_id, display_order);

-- Index cho primary image lookup
CREATE INDEX idx_product_image_primary ON product_images(product_id, is_primary);
```

### 2.3. Index Maintenance Strategy

**Monitoring Index Usage**
```sql
-- MySQL: Check index usage
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'web3'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- Check unused indexes
SELECT 
    object_schema,
    object_name,
    index_name
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE index_name IS NOT NULL
AND count_star = 0
AND object_schema = 'web3';
```

**Index Rebuild Schedule**
```sql
-- Rebuild indexes monthly
OPTIMIZE TABLE orders;
OPTIMIZE TABLE products;
OPTIMIZE TABLE financial_transaction;
```

## 3. Caching Opportunities

### 3.1. Application-Level Caching

**Spring Cache Configuration**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("products"),
            new ConcurrentMapCache("categories"),
            new ConcurrentMapCache("provinces"),
            new ConcurrentMapCache("districts"),
            new ConcurrentMapCache("wards")
        ));
        return cacheManager;
    }
}
```

**Cacheable Methods**
```java
@Service
public class ProductService {
    
    // Cache product list (TTL: 5 minutes)
    @Cacheable(value = "products", key = "'all'")
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAllAsDTO();
    }
    
    // Cache single product (TTL: 10 minutes)
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository.findByIdAsDTO(id);
    }
    
    // Evict cache on update
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long id, Product product) {
        return productRepository.save(product);
    }
}
```

### 3.2. Second-Level Cache (Hibernate)

**Configuration**
```properties
# application.properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

**Entity Caching**
```java
@Entity
@Table(name = "categories")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Category {
    // Rarely changes, good candidate for caching
}

@Entity
@Table(name = "products")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product {
    // Frequently read, occasionally updated
}
```

### 3.3. Query Result Caching

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
}
```

### 3.4. Redis Cache (Future Enhancement)

**Configuration**
```java
@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

**Use Cases for Redis**:
- Session storage
- Shopping cart data
- Product catalog cache
- Rate limiting
- Real-time inventory counts

## 4. Lazy Loading vs Eager Loading

### 4.1. Phân Tích Fetch Strategy Hiện Tại

**Order Entity**
```java
@ManyToOne(fetch = FetchType.LAZY)  // ✓ Correct
@JoinColumn(name = "customer_id", nullable = false)
private Customer customer;

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items;  // Default LAZY for OneToMany ✓
```

**Vấn đề**: Khi serialize Order sang JSON, có thể gặp LazyInitializationException

### 4.2. Best Practices

**Rule 1: Default to LAZY**
```java
// Always use LAZY for @ManyToOne
@ManyToOne(fetch = FetchType.LAZY)
private Category category;

// OneToMany is LAZY by default
@OneToMany(mappedBy = "product")
private List<ProductImage> images;
```

**Rule 2: Use EAGER Sparingly**
```java
// Only use EAGER for small, always-needed data
@ManyToOne(fetch = FetchType.EAGER)
private OrderStatus status;  // Enum, always needed
```

**Rule 3: Use JOIN FETCH for Specific Queries**
```java
@Query("SELECT o FROM Order o " +
       "JOIN FETCH o.customer " +
       "JOIN FETCH o.items " +
       "WHERE o.id = :id")
Optional<Order> findByIdWithDetails(@Param("id") Long id);
```

### 4.3. DTO Pattern để Tránh Lazy Loading Issues

**OrderDTO**
```java
@Data
@Builder
public class OrderDTO {
    private Long id;
    private String orderCode;
    private String customerName;
    private List<OrderItemDTO> items;
    private Double total;
    private OrderStatus status;
    
    public static OrderDTO from(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderCode(order.getOrderCode())
            .customerName(order.getCustomer().getName())  // Loaded in query
            .items(order.getItems().stream()
                .map(OrderItemDTO::from)
                .collect(Collectors.toList()))
            .total(order.getTotal())
            .status(order.getStatus())
            .build();
    }
}
```

**Repository với DTO Projection**
```java
@Query("SELECT new com.doan.WEB_TMDT.dto.OrderDTO(" +
       "o.id, o.orderCode, c.name, o.total, o.status) " +
       "FROM Order o JOIN o.customer c")
List<OrderDTO> findAllAsDTO();
```

### 4.4. Entity Graph

**Định nghĩa Entity Graph**
```java
@Entity
@NamedEntityGraph(
    name = "Order.withCustomerAndItems",
    attributeNodes = {
        @NamedAttributeNode("customer"),
        @NamedAttributeNode("items")
    }
)
public class Order {
    // ...
}
```

**Sử dụng Entity Graph**
```java
@EntityGraph(value = "Order.withCustomerAndItems")
Optional<Order> findById(Long id);
```

## 5. Pagination Strategy

### 5.1. Offset-Based Pagination (Hiện Tại)

**Implementation**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public Page<ProductDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        
        return productService.getAllProducts(pageable);
    }
}
```

**Vấn đề với Offset Pagination**:
- Performance giảm với offset lớn (page 1000+)
- Database phải scan và skip nhiều rows
- Không consistent khi data thay đổi (missing/duplicate items)

**Query Performance**:
```sql
-- Page 1: Fast
SELECT * FROM products ORDER BY id DESC LIMIT 20 OFFSET 0;

-- Page 1000: Slow (scan 20,000 rows)
SELECT * FROM products ORDER BY id DESC LIMIT 20 OFFSET 20000;
```

### 5.2. Cursor-Based Pagination (Keyset Pagination)

**Implementation**
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @GetMapping
    public CursorPage<OrderDTO> getOrders(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        
        return orderService.getOrdersAfterCursor(cursor, size);
    }
}

@Service
public class OrderService {
    
    public CursorPage<OrderDTO> getOrdersAfterCursor(Long cursor, int size) {
        List<Order> orders;
        
        if (cursor == null) {
            // First page
            orders = orderRepository.findTopNByOrderByIdDesc(size + 1);
        } else {
            // Subsequent pages
            orders = orderRepository.findByIdLessThanOrderByIdDesc(cursor, size + 1);
        }
        
        boolean hasNext = orders.size() > size;
        if (hasNext) {
            orders = orders.subList(0, size);
        }
        
        Long nextCursor = hasNext ? orders.get(orders.size() - 1).getId() : null;
        
        return new CursorPage<>(
            orders.stream().map(OrderDTO::from).collect(Collectors.toList()),
            nextCursor,
            hasNext
        );
    }
}
```

**Repository**
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o WHERE o.id < :cursor ORDER BY o.id DESC")
    List<Order> findByIdLessThanOrderByIdDesc(
        @Param("cursor") Long cursor, 
        Pageable pageable);
    
    List<Order> findTopNByOrderByIdDesc(int limit);
}
```

**Advantages**:
- Constant performance regardless of page depth
- Consistent results even when data changes
- Better for infinite scroll UIs

### 5.3. Hybrid Approach

**Use Offset for Admin Panels** (need page numbers)
```java
@GetMapping("/admin/orders")
public Page<OrderDTO> getOrdersForAdmin(Pageable pageable) {
    return orderService.getAllOrders(pageable);
}
```

**Use Cursor for Customer-Facing** (infinite scroll)
```java
@GetMapping("/customer/orders")
public CursorPage<OrderDTO> getOrdersForCustomer(
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10") int size) {
    return orderService.getOrdersAfterCursor(cursor, size);
}
```

### 5.4. Pagination Best Practices

**1. Limit Maximum Page Size**
```java
@GetMapping
public Page<ProductDTO> getProducts(
        @RequestParam(defaultValue = "20") int size) {
    
    // Prevent abuse
    if (size > 100) {
        size = 100;
    }
    
    return productService.getAllProducts(PageRequest.of(0, size));
}
```

**2. Add Total Count Optimization**
```java
// Expensive: Count all rows
Page<Product> page = productRepository.findAll(pageable);
long total = page.getTotalElements();  // SELECT COUNT(*) - slow!

// Better: Only count when needed
@GetMapping
public ResponseEntity<?> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "false") boolean includeCount) {
    
    Slice<ProductDTO> slice = productService.getProducts(PageRequest.of(page, size));
    
    if (includeCount) {
        long total = productService.countProducts();
        return ResponseEntity.ok(new PageResponse(slice.getContent(), total));
    }
    
    return ResponseEntity.ok(slice.getContent());
}
```

**3. Use Slice Instead of Page**
```java
// Page: Executes COUNT query
Page<Product> page = productRepository.findAll(pageable);

// Slice: No COUNT query, only checks if hasNext
Slice<Product> slice = productRepository.findAll(pageable);
```

## 6. Connection Pooling

### 6.1. HikariCP Configuration (Default in Spring Boot)

**Current Configuration**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/web3
spring.datasource.username=root
spring.datasource.password=
```

**Optimized Configuration**
```properties
# HikariCP Settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=WebTMDTHikariPool

# Connection test query
spring.datasource.hikari.connection-test-query=SELECT 1

# Leak detection (development only)
spring.datasource.hikari.leak-detection-threshold=60000
```

### 6.2. Pool Size Calculation

**Formula**: 
```
connections = ((core_count * 2) + effective_spindle_count)
```

**Example**:
- 4 CPU cores
- 1 disk (SSD = 1 spindle)
- Optimal pool size = (4 * 2) + 1 = 9 connections

**Recommendations**:
- Start with 10-20 connections
- Monitor connection usage
- Adjust based on actual load

### 6.3. Connection Pool Monitoring

**Metrics to Track**
```java
@Component
public class HikariMetrics {
    
    @Autowired
    private HikariDataSource hikariDataSource;
    
    @Scheduled(fixedRate = 60000)
    public void logPoolStats() {
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        
        log.info("HikariCP Stats:");
        log.info("  Active Connections: {}", poolMXBean.getActiveConnections());
        log.info("  Idle Connections: {}", poolMXBean.getIdleConnections());
        log.info("  Total Connections: {}", poolMXBean.getTotalConnections());
        log.info("  Threads Awaiting: {}", poolMXBean.getThreadsAwaitingConnection());
    }
}
```

**Warning Signs**:
- `ThreadsAwaitingConnection > 0`: Pool exhausted
- `ActiveConnections == MaximumPoolSize`: Need more connections
- `IdleConnections == 0`: Pool too small

### 6.4. Connection Leak Prevention

**Best Practices**
```java
// ✓ Good: Use @Transactional
@Transactional
public void processOrder(Order order) {
    orderRepository.save(order);
    // Connection automatically returned
}

// ✗ Bad: Manual connection management
public void processOrder(Order order) {
    Connection conn = dataSource.getConnection();
    // If exception occurs, connection leaks!
    // ...
}

// ✓ Good: Try-with-resources
public void rawQuery() {
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM orders")) {
        // Connection automatically closed
    }
}
```

**Leak Detection**
```properties
# Enable in development
spring.datasource.hikari.leak-detection-threshold=10000

# Logs warning if connection held > 10 seconds
```

## 7. Performance Monitoring & Metrics

### 7.1. Query Performance Logging

**Enable SQL Logging**
```properties
# Show SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Show bind parameters
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Show query execution time
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type=TRACE
```

**Slow Query Logging**
```properties
# MySQL slow query log
spring.datasource.url=jdbc:mysql://localhost:3306/web3?logger=Slf4JLogger&profileSQL=true
```

### 7.2. Performance Metrics

**Spring Boot Actuator**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.enable.jvm=true
management.metrics.enable.hikaricp=true
```

**Custom Metrics**
```java
@Component
public class PerformanceMetrics {
    
    private final MeterRegistry meterRegistry;
    
    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(Timer.builder("api.request.duration")
                .tag("method", joinPoint.getSignature().getName())
                .register(meterRegistry));
        }
    }
}
```

## 8. Tổng Kết và Khuyến Nghị

### 8.1. Priority 1 - Immediate Actions

1. **Add Critical Indexes**
   - `idx_orders_status` on orders(status)
   - `idx_orders_customer_id` on orders(customer_id)
   - `idx_export_order_id` on export_orders(order_id)
   - `idx_financial_date` on financial_transaction(transaction_date)

2. **Fix N+1 Queries**
   - Use JOIN FETCH in ProductService.getAll()
   - Add @EntityGraph to frequently used queries

3. **Optimize HikariCP**
   - Set maximum-pool-size=20
   - Set minimum-idle=5
   - Enable leak detection in development

### 8.2. Priority 2 - Short Term (1-2 months)

1. **Implement Caching**
   - Add Spring Cache for products and categories
   - Cache province/district/ward data
   - Implement query result caching

2. **Pagination Improvements**
   - Implement cursor-based pagination for customer-facing APIs
   - Use Slice instead of Page where total count not needed
   - Limit maximum page size to 100

3. **Batch Operations**
   - Enable Hibernate batch insert/update
   - Optimize Excel import with batch processing

### 8.3. Priority 3 - Long Term (3-6 months)

1. **Redis Integration**
   - Session storage
   - Shopping cart caching
   - Rate limiting
   - Real-time inventory cache

2. **Database Optimization**
   - Implement read replicas for reporting
   - Partition large tables (orders, financial_transaction)
   - Archive old data

3. **Advanced Monitoring**
   - Set up APM (Application Performance Monitoring)
   - Implement distributed tracing
   - Create performance dashboards

### 8.4. Performance Targets

**Response Time Goals**:
- API endpoints: < 200ms (p95)
- Database queries: < 50ms (p95)
- Page load: < 2 seconds

**Throughput Goals**:
- Support 1000 concurrent users
- Handle 100 orders/minute
- Process 10,000 products in catalog

**Resource Utilization**:
- Database connections: < 80% of pool
- CPU usage: < 70% average
- Memory usage: < 80% of allocated

## Kết Luận

Việc tối ưu hóa hiệu suất là một quá trình liên tục. Các chiến lược được đề xuất trong tài liệu này sẽ giúp hệ thống:

1. **Giảm thời gian phản hồi** thông qua indexing và query optimization
2. **Tăng throughput** với connection pooling và caching
3. **Cải thiện trải nghiệm người dùng** với pagination và lazy loading
4. **Dễ dàng scale** khi lượng dữ liệu và traffic tăng

Quan trọng nhất là **đo lường trước khi tối ưu** và **monitor sau khi deploy** để đảm bảo các thay đổi mang lại hiệu quả thực sự.
