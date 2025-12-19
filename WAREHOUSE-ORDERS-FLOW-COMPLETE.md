# Luồng Quản Lý Đơn Hàng Warehouse - Hoàn Chỉnh

## Tổng Quan Luồng

```
1. Khách đặt hàng
   ↓
2. Thanh toán (COD hoặc Online)
   ↓
3. Order status = CONFIRMED (Đã xác nhận, chờ xuất kho)
   ↓ [Warehouse staff xuất kho]
4. Order status = SHIPPING (Đã xuất kho, đợi tài xế)
   ↓ [Tài xế giao hàng]
5. Order status = DELIVERED (Đã giao hàng)
```

## API Endpoints

### 1. Lấy đơn chờ xuất kho (Tab "Chờ xuất kho")
```
GET /api/inventory/orders/pending-export
Authorization: Bearer <TOKEN>
Permission: ADMIN hoặc WAREHOUSE

Response:
{
  "success": true,
  "data": [
    {
      "orderId": 1,
      "orderCode": "ORD001",
      "status": "CONFIRMED",  ← Chỉ trả về CONFIRMED
      ...
    }
  ]
}
```

**Backend Logic:**
```java
@GetMapping("/pending-export")
public ApiResponse getOrdersPendingExport(...) {
    return orderService.getAllOrders("CONFIRMED", page, size);
}
```

### 2. Lấy đơn đã xuất kho (Tab "Đã xuất kho")
```
GET /api/inventory/orders/exported
Authorization: Bearer <TOKEN>
Permission: ADMIN hoặc WAREHOUSE

Response:
{
  "success": true,
  "data": [
    {
      "orderId": 2,
      "orderCode": "ORD002",
      "status": "SHIPPING",  ← Chỉ trả về SHIPPING
      "ghnOrderCode": "GHN123",
      ...
    }
  ]
}
```

**Backend Logic:**
```java
@GetMapping("/exported")
public ApiResponse getOrdersExported(...) {
    return orderService.getAllOrders("SHIPPING", page, size);
}
```

## OrderService.getAllOrders() Logic

```java
public ApiResponse getAllOrders(String status, int page, int size) {
    List<Order> orders;
    
    if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
        try {
            // Convert string to enum
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            
            // Query database
            orders = orderRepository.findByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Trạng thái không hợp lệ");
        }
    } else {
        orders = orderRepository.findAll();
    }
    
    // Sort by created date desc
    orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
    
    // Convert to DTO
    List<OrderResponse> responses = orders.stream()
            .map(this::toOrderResponse)
            .collect(Collectors.toList());
    
    return ApiResponse.success("Danh sách đơn hàng", responses);
}
```

## Frontend Logic

```typescript
const fetchOrders = async () => {
  const token = localStorage.getItem('token');
  
  let url = '';
  if (activeTab === 'pending') {
    url = 'http://localhost:8080/api/inventory/orders/pending-export';
  } else {
    url = 'http://localhost:8080/api/inventory/orders/exported';
  }
  
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Double check filter
    const filteredOrders = (result.data || []).filter((order) => {
      if (activeTab === 'pending') {
        return order.status === 'CONFIRMED';
      } else {
        return order.status === 'SHIPPING';
      }
    });
    setOrders(filteredOrders);
  }
};
```

## Debug: Tại Sao API Trả Về List Rỗng?

### Nguyên nhân có thể:

#### 1. Không có đơn hàng nào có status = SHIPPING
**Kiểm tra:**
```sql
SELECT * FROM orders WHERE status = 'SHIPPING';
```

**Nếu rỗng** → Chưa có đơn nào được xuất kho

**Giải pháp:**
- Xuất kho một đơn hàng để test
- Hoặc update manual: `UPDATE orders SET status = 'SHIPPING' WHERE id = 1;`

#### 2. Status trong database khác với enum
**Kiểm tra:**
```sql
SELECT DISTINCT status FROM orders;
```

**Kết quả mong đợi:**
```
PENDING_PAYMENT
CONFIRMED
SHIPPING
DELIVERED
CANCELLED
```

**Nếu khác** → Database có giá trị không khớp enum

#### 3. Repository method không hoạt động
**Test:**
```java
// Trong OrderServiceImpl
List<Order> allOrders = orderRepository.findAll();
log.info("Total orders: {}", allOrders.size());

List<Order> shippingOrders = orderRepository.findByStatus(OrderStatus.SHIPPING);
log.info("Shipping orders: {}", shippingOrders.size());
```

#### 4. Permission issue
**Kiểm tra token:**
- Token có hợp lệ không?
- User có role WAREHOUSE không?

## Test Cases

### Test 1: Kiểm tra database
```sql
-- Xem tất cả đơn hàng và status
SELECT id, order_code, status, ghn_order_code, created_at 
FROM orders 
ORDER BY created_at DESC;

-- Đếm theo status
SELECT status, COUNT(*) as count 
FROM orders 
GROUP BY status;

-- Tìm đơn CONFIRMED (chờ xuất)
SELECT * FROM orders WHERE status = 'CONFIRMED';

-- Tìm đơn SHIPPING (đã xuất)
SELECT * FROM orders WHERE status = 'SHIPPING';
```

### Test 2: Test API trực tiếp
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"warehouse@example.com","password":"password123"}'

# Get token from response
TOKEN="<your_token>"

# Test pending orders
curl http://localhost:8080/api/inventory/orders/pending-export \
  -H "Authorization: Bearer $TOKEN"

# Test exported orders
curl http://localhost:8080/api/inventory/orders/exported \
  -H "Authorization: Bearer $TOKEN"
```

### Test 3: Tạo đơn test
```sql
-- Tạo đơn CONFIRMED (chờ xuất)
INSERT INTO orders (order_code, customer_id, status, payment_status, payment_method, 
                    shipping_address, province, district, ward, address,
                    subtotal, shipping_fee, discount, total, created_at, confirmed_at)
VALUES ('ORD_TEST_001', 1, 'CONFIRMED', 'PAID', 'COD',
        'Test Address', 'Hà Nội', 'Hà Đông', '20308', 'Số 1',
        1000000, 30000, 0, 1030000, NOW(), NOW());

-- Tạo đơn SHIPPING (đã xuất)
INSERT INTO orders (order_code, customer_id, status, payment_status, payment_method,
                    shipping_address, province, district, ward, address,
                    subtotal, shipping_fee, discount, total, 
                    ghn_order_code, ghn_shipping_status,
                    created_at, confirmed_at, shipped_at)
VALUES ('ORD_TEST_002', 1, 'SHIPPING', 'PAID', 'COD',
        'Test Address', 'Hà Nội', 'Hà Đông', '20308', 'Số 2',
        2000000, 30000, 0, 2030000,
        'GHN_TEST_123', 'created',
        NOW(), NOW(), NOW());
```

## Giải Pháp Nếu Vẫn Rỗng

### Option 1: Thêm logging
```java
@GetMapping("/exported")
public ApiResponse getOrdersExported(...) {
    log.info("🔍 Getting exported orders (SHIPPING status)");
    
    ApiResponse response = orderService.getAllOrders("SHIPPING", page, size);
    
    log.info("📦 Found {} orders", 
        response.getData() != null ? ((List)response.getData()).size() : 0);
    
    return response;
}
```

### Option 2: Trả về tất cả orders để debug
```java
@GetMapping("/debug-all")
@PreAuthorize("hasAuthority('ADMIN')")
public ApiResponse getAllOrdersDebug() {
    List<Order> allOrders = orderRepository.findAll();
    
    Map<String, Object> debug = new HashMap<>();
    debug.put("total", allOrders.size());
    debug.put("byStatus", allOrders.stream()
        .collect(Collectors.groupingBy(
            o -> o.getStatus().toString(),
            Collectors.counting()
        )));
    
    return ApiResponse.success("Debug info", debug);
}
```

### Option 3: Fix data nếu cần
```sql
-- Nếu có đơn có ghnOrderCode nhưng status không phải SHIPPING
UPDATE orders 
SET status = 'SHIPPING', 
    shipped_at = ghn_created_at
WHERE ghn_order_code IS NOT NULL 
  AND status != 'SHIPPING';
```

## Checklist Debug

- [ ] Kiểm tra database có đơn hàng không
- [ ] Kiểm tra có đơn nào status = SHIPPING không
- [ ] Kiểm tra token có hợp lệ không
- [ ] Kiểm tra user có permission WAREHOUSE không
- [ ] Kiểm tra API response (console.log)
- [ ] Kiểm tra backend log
- [ ] Test API bằng Postman/curl
- [ ] Kiểm tra OrderRepository.findByStatus() hoạt động không

## Kết Luận

Nếu API trả về list rỗng, 99% là do:
1. **Không có data**: Chưa có đơn nào được xuất kho (status = SHIPPING)
2. **Data sai**: Status trong DB không khớp với enum

**Giải pháp nhanh nhất:**
1. Xuất kho một đơn hàng để tạo data test
2. Hoặc chạy SQL insert data test
3. Refresh lại trang warehouse orders
