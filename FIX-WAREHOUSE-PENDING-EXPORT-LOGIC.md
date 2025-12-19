# Fix Warehouse Pending Export Logic

## Vấn Đề

Trước đây, tab "Chờ xuất kho" hiển thị **TẤT CẢ** đơn có status = `CONFIRMED`, kể cả những đơn đã xuất kho rồi (đã có trong bảng `export_orders`).

→ Gây nhầm lẫn vì đơn đã xuất vẫn hiển thị ở "Chờ xuất kho"

## Giải Pháp

Thêm logic: **Chỉ hiển thị đơn CONFIRMED mà CHƯA có trong bảng `export_orders`**

## Thay Đổi

### 1. OrderRepository - Thêm Query Mới

**File**: `src/main/java/com/doan/WEB_TMDT/module/order/repository/OrderRepository.java`

```java
// Warehouse queries - Lấy đơn CONFIRMED chưa xuất kho
@Query("SELECT o FROM Order o WHERE o.status = :status " +
       "AND NOT EXISTS (SELECT 1 FROM ExportOrder e WHERE e.orderId = o.id) " +
       "ORDER BY o.confirmedAt DESC")
List<Order> findByStatusAndNotExported(@Param("status") OrderStatus status);
```

**Giải thích**:
- `NOT EXISTS`: Chỉ lấy đơn mà KHÔNG tồn tại trong `export_orders`
- `e.orderId = o.id`: Join điều kiện
- `ORDER BY o.confirmedAt DESC`: Đơn mới xác nhận lên trước

### 2. OrderService - Thêm Method Mới

**File**: `src/main/java/com/doan/WEB_TMDT/module/order/service/OrderService.java`

```java
ApiResponse getOrdersPendingExport(); // Đơn CONFIRMED chưa xuất kho
```

### 3. OrderServiceImpl - Implement Method

**File**: `src/main/java/com/doan/WEB_TMDT/module/order/service/impl/OrderServiceImpl.java`

```java
@Override
public ApiResponse getOrdersPendingExport() {
    // Lấy các đơn CONFIRMED mà chưa có trong export_orders
    List<Order> orders = orderRepository.findByStatusAndNotExported(OrderStatus.CONFIRMED);
    
    List<OrderResponse> responses = orders.stream()
            .map(this::toOrderResponse)
            .toList();
    
    log.info("Found {} orders pending export (CONFIRMED and not exported yet)", orders.size());
    return ApiResponse.success("Danh sách đơn hàng chờ xuất kho", responses);
}
```

### 4. InventoryOrderController - Update Endpoint

**File**: `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryOrderController.java`

**Trước**:
```java
@GetMapping("/pending-export")
public ApiResponse getOrdersPendingExport(
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size) {
    // Lấy TẤT CẢ đơn CONFIRMED (kể cả đã xuất)
    return orderService.getAllOrders("CONFIRMED", page, size);
}
```

**Sau**:
```java
@GetMapping("/pending-export")
public ApiResponse getOrdersPendingExport() {
    // Lấy các đơn hàng đã CONFIRMED, chưa xuất kho (chưa có trong export_orders)
    return orderService.getOrdersPendingExport();
}
```

## Luồng Hoạt Động

### Trước Khi Fix:

```
Tab "Chờ xuất kho":
- Đơn #1: CONFIRMED, chưa xuất ✅
- Đơn #2: CONFIRMED, đã xuất ❌ (Sai! Không nên hiển thị)
- Đơn #3: CONFIRMED, chưa xuất ✅
```

### Sau Khi Fix:

```
Tab "Chờ xuất kho":
- Đơn #1: CONFIRMED, chưa xuất ✅
- Đơn #3: CONFIRMED, chưa xuất ✅

Tab "Đã xuất kho":
- Đơn #2: READY_TO_SHIP, đã xuất ✅
```

## Cách Hoạt Động

1. **Khi warehouse xuất kho**:
   - Tạo record trong `export_orders` với `order_id = X`
   - Update order status: `CONFIRMED` → `READY_TO_SHIP`

2. **Khi load tab "Chờ xuất kho"**:
   - Query: Lấy đơn `CONFIRMED` AND `NOT EXISTS trong export_orders`
   - Kết quả: Chỉ đơn chưa xuất

3. **Khi load tab "Đã xuất kho"**:
   - Query: Lấy đơn `READY_TO_SHIP`
   - Kết quả: Chỉ đơn đã xuất

## Database Schema

```sql
-- Bảng orders
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    order_code VARCHAR(50),
    status VARCHAR(20), -- CONFIRMED, READY_TO_SHIP, SHIPPING, ...
    ...
);

-- Bảng export_orders
CREATE TABLE export_orders (
    id BIGINT PRIMARY KEY,
    export_code VARCHAR(50),
    order_id BIGINT, -- ← Link đến orders.id
    status VARCHAR(20),
    ...
);
```

**Quan hệ**: `export_orders.order_id` → `orders.id` (1-1 hoặc 1-n nếu xuất nhiều lần)

## Testing

### Test 1: Đơn Chưa Xuất
```
1. Tạo đơn mới → Status = CONFIRMED
2. Vào tab "Chờ xuất kho"
3. ✅ Thấy đơn hiển thị
4. Vào tab "Đã xuất kho"
5. ✅ KHÔNG thấy đơn
```

### Test 2: Đơn Đã Xuất
```
1. Xuất kho đơn #1
2. Vào tab "Chờ xuất kho"
3. ✅ Đơn #1 BIẾN MẤT
4. Vào tab "Đã xuất kho"
5. ✅ Đơn #1 XUẤT HIỆN
```

### Test 3: Query Database
```sql
-- Kiểm tra đơn CONFIRMED chưa xuất
SELECT o.id, o.order_code, o.status
FROM orders o
WHERE o.status = 'CONFIRMED'
  AND NOT EXISTS (
    SELECT 1 FROM export_orders e 
    WHERE e.order_id = o.id
  );

-- Kiểm tra đơn đã xuất
SELECT o.id, o.order_code, e.export_code
FROM orders o
JOIN export_orders e ON e.order_id = o.id
WHERE o.status = 'READY_TO_SHIP';
```

## Lợi Ích

1. **Chính xác**: Chỉ hiển thị đơn thực sự cần xuất
2. **Không trùng lặp**: Đơn đã xuất không còn ở "Chờ xuất kho"
3. **Dễ tracking**: Warehouse staff biết rõ đơn nào cần xử lý
4. **Tránh nhầm lẫn**: Không xuất kho 2 lần cho cùng 1 đơn

## Files Đã Thay Đổi

1. ✅ `src/main/java/com/doan/WEB_TMDT/module/order/repository/OrderRepository.java`
2. ✅ `src/main/java/com/doan/WEB_TMDT/module/order/service/OrderService.java`
3. ✅ `src/main/java/com/doan/WEB_TMDT/module/order/service/impl/OrderServiceImpl.java`
4. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryOrderController.java`

## Lưu Ý

- Không cần migration database (chỉ thay đổi query logic)
- Không ảnh hưởng đến các module khác
- Frontend không cần thay đổi (vẫn gọi cùng API endpoint)
- Backward compatible (đơn cũ vẫn hoạt động bình thường)

## Kết Luận

✅ **HOÀN THÀNH** - Tab "Chờ xuất kho" giờ chỉ hiển thị đơn CONFIRMED mà chưa có trong `export_orders`
