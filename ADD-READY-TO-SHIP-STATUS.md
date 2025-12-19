# Thêm Status READY_TO_SHIP

## Vấn Đề
Trước đây chỉ có 1 status `SHIPPING` cho cả 2 trạng thái:
- Đã xuất kho, đợi tài xế lấy
- Tài xế đang giao hàng

→ Không phân biệt được, gây nhầm lẫn

## Giải Pháp
Thêm status mới `READY_TO_SHIP` để tách biệt:

### Luồng Cũ (Sai):
```
CONFIRMED → SHIPPING → DELIVERED
(Đã xác nhận) → (Đang giao???) → (Đã giao)
```

### Luồng Mới (Đúng):
```
CONFIRMED → READY_TO_SHIP → SHIPPING → DELIVERED
(Đã xác nhận) → (Đã xuất kho) → (Đang giao) → (Đã giao)
```

## Chi Tiết Các Status

| Status | Ý Nghĩa | Khi Nào |
|--------|---------|---------|
| `PENDING_PAYMENT` | Chờ thanh toán | Đơn mới tạo (online) |
| `CONFIRMED` | Đã xác nhận, chờ xuất kho | Sau thanh toán hoặc COD |
| `READY_TO_SHIP` | **Đã xuất kho, đợi tài xế lấy** | Sau khi warehouse xuất kho |
| `SHIPPING` | **Tài xế đang giao hàng** | Tài xế đã lấy hàng |
| `DELIVERED` | Đã giao hàng | Giao thành công |
| `COMPLETED` | Hoàn thành | Đơn hoàn tất |
| `CANCELLED` | Đã hủy | Đơn bị hủy |

## Thay Đổi Code

### 1. OrderStatus Enum

**File**: `src/main/java/com/doan/WEB_TMDT/module/order/entity/OrderStatus.java`

```java
public enum OrderStatus {
    PENDING_PAYMENT, // Chờ thanh toán (đơn online)
    CONFIRMED,       // Đã xác nhận - Chờ xuất kho
    READY_TO_SHIP,   // Đã xuất kho - Chờ tài xế lấy hàng ← MỚI
    SHIPPING,        // Tài xế đang giao hàng
    DELIVERED,       // Đã giao hàng
    COMPLETED,       // Hoàn thành
    CANCELLED,       // Đã hủy
    RETURNED,        // Đã trả hàng
    PROCESSING       // Đang xử lý (deprecated)
}
```

### 2. InventoryServiceImpl - Xuất Kho

**File**: `src/main/java/com/doan/WEB_TMDT/module/inventory/service/impl/InventoryServiceImpl.java`

**Trước:**
```java
// Set status = SHIPPING sau khi xuất kho
order.setStatus(OrderStatus.SHIPPING);
```

**Sau:**
```java
// Set status = READY_TO_SHIP sau khi xuất kho
order.setStatus(OrderStatus.READY_TO_SHIP);
```

### 3. InventoryOrderController - API

**File**: `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryOrderController.java`

```java
@GetMapping("/exported")
public ApiResponse getOrdersExported(...) {
    // Lấy đơn đã xuất kho (READY_TO_SHIP)
    return orderService.getAllOrders("READY_TO_SHIP", page, size);
}
```

### 4. Frontend - Hiển Thị Text

**Files**: 
- `src/frontend/app/orders/[id]/page.tsx`
- `src/frontend/app/orders/page.tsx`

```typescript
case 'READY_TO_SHIP':
  return 'Đã chuẩn bị hàng - Đợi tài xế đến lấy'
case 'SHIPPING':
  return 'Đang giao hàng'
```

## Warehouse Orders Tabs

### Tab "Chờ xuất kho"
- API: `GET /api/inventory/orders/pending-export`
- Status: `CONFIRMED`
- Hiển thị: Đơn cần xuất kho

### Tab "Đã xuất kho"
- API: `GET /api/inventory/orders/exported`
- Status: `READY_TO_SHIP` ← Thay đổi
- Hiển thị: Đơn đã xuất, đợi tài xế

## Migration Database

Chạy file `add-ready-to-ship-status.sql`:

```sql
-- Update các đơn đã xuất kho sang READY_TO_SHIP
UPDATE orders
SET status = 'READY_TO_SHIP'
WHERE status = 'SHIPPING'
  AND ghn_order_code IS NOT NULL
  AND delivered_at IS NULL;
```

## GHN Webhook Integration

Khi GHN webhook báo tài xế đã lấy hàng:

```java
// Trong GHN webhook handler
if (ghnStatus.equals("picking") || ghnStatus.equals("delivering")) {
    // Tài xế đã lấy hàng → Chuyển sang SHIPPING
    order.setStatus(OrderStatus.SHIPPING);
    orderRepository.save(order);
}
```

## Timeline Đầy Đủ

### Từ Góc Nhìn Khách Hàng:

```
1. Đặt hàng
   ↓
2. Thanh toán
   Status: CONFIRMED
   Text: "Đã xác nhận - Đang chuẩn bị hàng"
   ↓
3. Warehouse xuất kho
   Status: READY_TO_SHIP
   Text: "Đã chuẩn bị hàng - Đợi tài xế đến lấy"
   ↓
4. Tài xế lấy hàng
   Status: SHIPPING
   Text: "Đang giao hàng"
   ↓
5. Giao thành công
   Status: DELIVERED
   Text: "Đã giao hàng"
```

### Từ Góc Nhìn Warehouse:

```
Tab "Chờ xuất kho":
- Hiển thị đơn CONFIRMED
- Có nút "Xuất kho"

Tab "Đã xuất kho":
- Hiển thị đơn READY_TO_SHIP
- Đã xuất, đợi tài xế
- Không có nút xuất nữa
```

### Từ Góc Nhìn Shipper:

```
Danh sách đơn cần giao:
- Hiển thị đơn READY_TO_SHIP
- Tài xế nhận đơn → Chuyển sang SHIPPING
```

## Testing

### Test 1: Xuất kho
```
1. Vào warehouse orders
2. Tab "Chờ xuất kho"
3. Click "Xuất kho" một đơn
4. Nhập serial, xác nhận
5. Kiểm tra:
   ✓ Đơn biến mất khỏi tab "Chờ xuất kho"
   ✓ Đơn xuất hiện ở tab "Đã xuất kho"
   ✓ Status = READY_TO_SHIP
```

### Test 2: Khách hàng xem đơn
```
1. Login khách hàng
2. Vào /orders
3. Click đơn vừa xuất kho
4. Kiểm tra:
   ✓ Hiển thị "Đã chuẩn bị hàng - Đợi tài xế đến lấy"
   ✓ Có mã GHN
```

### Test 3: Tài xế nhận đơn (TODO)
```
1. Login shipper
2. Xem danh sách đơn READY_TO_SHIP
3. Nhận đơn
4. Status chuyển sang SHIPPING
```

## Lưu Ý

1. **Backward Compatibility**: Các đơn cũ có status = SHIPPING cần migration
2. **GHN Webhook**: Cần implement logic chuyển READY_TO_SHIP → SHIPPING
3. **Shipper Module**: Cần update để hiển thị đơn READY_TO_SHIP
4. **Accounting**: Kiểm tra các event listener có bị ảnh hưởng không

## Files Đã Thay Đổi

1. ✅ `src/main/java/com/doan/WEB_TMDT/module/order/entity/OrderStatus.java`
2. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/service/impl/InventoryServiceImpl.java`
3. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryOrderController.java`
4. ✅ `src/frontend/app/warehouse/orders/page.tsx`
5. ✅ `src/frontend/app/orders/[id]/page.tsx`
6. ✅ `src/frontend/app/orders/page.tsx`

## TODO

- [x] Implement GHN webhook handler để chuyển READY_TO_SHIP → SHIPPING ✅
- [x] Update shipper module để hiển thị đơn READY_TO_SHIP ✅
- [x] Thêm button "Nhận đơn" cho shipper ✅
- [ ] Migration data cũ (chạy file `add-ready-to-ship-status.sql`)
- [ ] Test complete flow end-to-end
