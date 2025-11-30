# API Backend cần bổ sung

## 1. Sales Controller - Đã tạo file mới

**File:** `src/main/java/com/doan/WEB_TMDT/module/order/controller/SalesController.java`

### Endpoints cần implement trong OrderService:

```java
// 1. Thống kê cho dashboard sales
ApiResponse getSalesStats();

// 2. Lấy đơn hàng theo status
ApiResponse getOrdersByStatus(String status);

// 3. Xuất kho cho đơn hàng
ApiResponse exportOrderForSales(Long orderId);
```

## 2. Cập nhật OrderService Interface

**File:** `src/main/java/com/doan/WEB_TMDT/module/order/service/OrderService.java`

**Thêm methods:**
```java
// Sales endpoints
ApiResponse getSalesStats();
ApiResponse getOrdersByStatus(String status);
ApiResponse exportOrderForSales(Long orderId);
```

## 3. Implement trong OrderServiceImpl

**File:** `src/main/java/com/doan/WEB_TMDT/module/order/service/impl/OrderServiceImpl.java`

### Method 1: getSalesStats()
```java
@Override
public ApiResponse getSalesStats() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    
    // Tổng đơn hàng trong tháng
    long totalOrders = orderRepository.countByCreatedAtBetween(startOfMonth, now);
    
    // Đơn chờ xuất kho (CONFIRMED)
    long pendingExport = orderRepository.countByStatus(OrderStatus.CONFIRMED);
    
    // Đơn đang giao (SHIPPING)
    long shipped = orderRepository.countByStatus(OrderStatus.SHIPPING);
    
    // Tổng doanh thu trong tháng
    Double totalRevenue = orderRepository.sumTotalByDateRange(startOfMonth, now);
    
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalOrders", totalOrders);
    stats.put("pendingExport", pendingExport);
    stats.put("shipped", shipped);
    stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);
    
    return ApiResponse.success("Thống kê bán hàng", stats);
}
```

### Method 2: getOrdersByStatus()
```java
@Override
public ApiResponse getOrdersByStatus(String status) {
    List<Order> orders;
    
    if (status == null || "ALL".equals(status)) {
        orders = orderRepository.findAll();
    } else {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        orders = orderRepository.findByStatus(orderStatus);
    }
    
    List<OrderResponse> responses = orders.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    
    return ApiResponse.success("Danh sách đơn hàng", responses);
}
```

### Method 3: exportOrderForSales()
```java
@Override
@Transactional
public ApiResponse exportOrderForSales(Long orderId) {
    // 1. Lấy đơn hàng
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    
    // 2. Kiểm tra trạng thái
    if (order.getStatus() != OrderStatus.CONFIRMED) {
        return ApiResponse.error("Chỉ có thể xuất kho đơn hàng đã xác nhận");
    }
    
    // 3. Kiểm tra tồn kho
    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        if (product.getStockQuantity() < item.getQuantity()) {
            return ApiResponse.error("Sản phẩm " + product.getName() + " không đủ tồn kho");
        }
    }
    
    // 4. Trừ tồn kho và lưu giá vốn
    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        
        // Trừ tồn kho
        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
        productRepository.save(product);
        
        // Lưu giá vốn (nếu có field purchasePrice trong OrderItem)
        // item.setPurchasePrice(product.getPurchasePrice());
    }
    
    // 5. Cập nhật trạng thái đơn hàng
    order.setStatus(OrderStatus.SHIPPING);
    order.setShippedAt(LocalDateTime.now());
    orderRepository.save(order);
    
    // 6. Tạo phiếu xuất kho (nếu cần)
    // TODO: Tạo ExportOrder trong module inventory
    
    return ApiResponse.success("Xuất kho thành công", convertToResponse(order));
}
```

## 4. Cập nhật OrderRepository

**File:** `src/main/java/com/doan/WEB_TMDT/module/order/repository/OrderRepository.java`

**Thêm methods:**
```java
// Đếm đơn hàng theo status
long countByStatus(OrderStatus status);

// Đếm đơn hàng trong khoảng thời gian
long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

// Tìm đơn hàng theo status
List<Order> findByStatus(OrderStatus status);

// Tổng doanh thu trong khoảng thời gian
@Query("SELECT SUM(o.total) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.paymentStatus = 'PAID'")
Double sumTotalByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
```

## 5. Cập nhật SecurityConfig

**File:** `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java`

**Thêm:**
```java
// Sales endpoints
.requestMatchers("/api/sales/**").hasAnyAuthority("ADMIN", "SALES")

// Cho phép SALES xem orders
.requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyAuthority("CUSTOMER", "ADMIN", "SALES")
```

## 6. Thêm field purchasePrice vào OrderItem (Quan trọng!)

**File:** `src/main/java/com/doan/WEB_TMDT/module/order/entity/OrderItem.java`

**Thêm field:**
```java
@Column(name = "purchase_price")
private Double purchasePrice; // Giá nhập tại thời điểm bán
```

**Cập nhật khi tạo OrderItem:**
```java
// Trong OrderServiceImpl.createOrderFromCart()
OrderItem orderItem = OrderItem.builder()
    .order(order)
    .product(cartItem.getProduct())
    .quantity(cartItem.getQuantity())
    .price(cartItem.getPrice())
    .subtotal(cartItem.getPrice() * cartItem.getQuantity())
    .purchasePrice(cartItem.getProduct().getPurchasePrice()) // LƯU GIÁ VỐN
    .build();
```

## 7. Migration SQL cho purchasePrice

**File mới:** `add_purchase_price_to_order_item.sql`

```sql
ALTER TABLE order_item 
ADD COLUMN purchase_price DOUBLE DEFAULT 0;

-- Cập nhật giá vốn cho các đơn cũ (giả định 60% giá bán)
UPDATE order_item 
SET purchase_price = price * 0.6 
WHERE purchase_price IS NULL OR purchase_price = 0;
```

## 8. Cập nhật AccountingServiceImpl

**File:** `src/main/java/com/doan/WEB_TMDT/module/accounting/service/impl/AccountingServiceImpl.java`

**Sửa method calculateOrderFinancials():**
```java
private Map<String, Object> calculateOrderFinancials(Order order) {
    Map<String, Object> report = new HashMap<>();
    
    double revenue = order.getTotal();
    double vat = revenue * 0.1; // VAT 10%
    
    // TÍNH GIÁ VỐN THỰC TẾ từ purchasePrice
    double costOfGoods = order.getItems().stream()
        .mapToDouble(item -> item.getQuantity() * item.getPurchasePrice())
        .sum();
    
    double shippingCost = order.getShippingFee();
    double paymentGatewayCost = revenue * 0.02; // Phí cổng thanh toán 2%
    
    double grossProfit = revenue - vat - costOfGoods - shippingCost - paymentGatewayCost;
    double corporateTax = grossProfit * 0.2; // Thuế TNDN 20%
    double netProfit = grossProfit - corporateTax;
    double actualReceived = revenue - paymentGatewayCost;
    
    report.put("orderId", order.getOrderCode());
    report.put("date", order.getCreatedAt().toLocalDate().toString());
    report.put("revenue", Math.round(revenue));
    report.put("vat", Math.round(vat));
    report.put("costOfGoods", Math.round(costOfGoods)); // GIÁ VỐN THỰC TẾ
    report.put("shippingCost", Math.round(shippingCost));
    report.put("paymentGatewayCost", Math.round(paymentGatewayCost));
    report.put("grossProfit", Math.round(grossProfit));
    report.put("corporateTax", Math.round(corporateTax));
    report.put("netProfit", Math.round(netProfit));
    report.put("actualReceived", Math.round(actualReceived));
    
    return report;
}
```

## Tóm tắt các file cần sửa/tạo:

### Tạo mới:
1. ✅ `SalesController.java` - Đã tạo
2. ⚠️ `add_purchase_price_to_order_item.sql` - Cần tạo

### Cập nhật:
3. ⚠️ `OrderService.java` - Thêm 3 methods
4. ⚠️ `OrderServiceImpl.java` - Implement 3 methods
5. ⚠️ `OrderRepository.java` - Thêm queries
6. ⚠️ `OrderItem.java` - Thêm field purchasePrice
7. ⚠️ `SecurityConfig.java` - Thêm /api/sales/**
8. ⚠️ `AccountingServiceImpl.java` - Sửa tính giá vốn

## Ưu tiên thực hiện:

### Cao (Cần ngay):
1. Thêm purchasePrice vào OrderItem + migration
2. Implement 3 methods trong OrderService
3. Cập nhật SecurityConfig

### Trung bình:
4. Sửa tính giá vốn trong AccountingService
5. Test toàn bộ flow

### Thấp (Có thể sau):
6. Tạo ExportOrder trong inventory module
7. Tích hợp với hệ thống kho
