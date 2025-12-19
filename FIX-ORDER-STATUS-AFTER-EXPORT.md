# Fix Cập Nhật Trạng Thái Đơn Hàng Sau Xuất Kho

## Vấn đề
Sau khi xuất kho thành công và tạo đơn GHN, đơn hàng vẫn ở trạng thái `CONFIRMED` (Đã xác nhận) thay vì chuyển sang `SHIPPING` (Đang giao hàng / Đợi tài xế lấy hàng).

## Nguyên nhân
Trong method `createGHNOrderForExport()`, code chỉ cập nhật thông tin GHN vào order:
- `ghnOrderCode`
- `ghnShippingStatus`
- `ghnCreatedAt`
- `ghnExpectedDeliveryTime`

Nhưng KHÔNG cập nhật `order.status` và `order.shippedAt`.

## Giải pháp

### File: `InventoryServiceImpl.java`

**Trước:**
```java
// Update order with GHN info
order.setGhnOrderCode(ghnResponse.getOrderCode());
order.setGhnShippingStatus("created");
order.setGhnCreatedAt(LocalDateTime.now());
order.setGhnExpectedDeliveryTime(ghnResponse.getExpectedDeliveryTime());
orderRepository.save(order);
```

**Sau:**
```java
// Update order with GHN info and change status to SHIPPING
order.setGhnOrderCode(ghnResponse.getOrderCode());
order.setGhnShippingStatus("created");
order.setGhnCreatedAt(LocalDateTime.now());
order.setGhnExpectedDeliveryTime(ghnResponse.getExpectedDeliveryTime());

// ✅ Update order status to SHIPPING (Đợi tài xế lấy hàng)
order.setStatus(com.doan.WEB_TMDT.module.order.entity.OrderStatus.SHIPPING);
order.setShippedAt(LocalDateTime.now());

orderRepository.save(order);
```

## Luồng Trạng Thái Đơn Hàng

### 1. Đơn COD (Thanh toán khi nhận hàng)
```
PENDING_PAYMENT (Tạo đơn)
    ↓
CONFIRMED (Tự động xác nhận ngay)
    ↓
SHIPPING (Sau khi xuất kho & tạo GHN) ← ✅ Fix này
    ↓
DELIVERED (GHN giao thành công)
    ↓
COMPLETED (Hoàn thành)
```

### 2. Đơn Online (SEPAY/VNPAY)
```
PENDING_PAYMENT (Tạo đơn, chờ thanh toán)
    ↓
CONFIRMED (Sau khi thanh toán thành công)
    ↓
SHIPPING (Sau khi xuất kho & tạo GHN) ← ✅ Fix này
    ↓
DELIVERED (GHN giao thành công)
    ↓
COMPLETED (Hoàn thành)
```

## OrderStatus Enum

```java
public enum OrderStatus {
    PENDING_PAYMENT, // Chờ thanh toán (đơn online)
    CONFIRMED,       // Đã xác nhận (chờ xuất kho)
    PROCESSING,      // Đang xử lý (không dùng)
    SHIPPING,        // Đang giao hàng (đợi tài xế lấy hàng) ← ✅ Status này
    DELIVERED,       // Đã giao hàng
    COMPLETED,       // Hoàn thành
    CANCELLED,       // Đã hủy
    RETURNED         // Đã trả hàng
}
```

## Các Trường Timestamp Liên Quan

| Trường | Ý nghĩa | Khi nào set |
|--------|---------|-------------|
| `createdAt` | Thời gian tạo đơn | Khi tạo đơn |
| `confirmedAt` | Thời gian xác nhận | Khi xác nhận đơn (COD: ngay lập tức, Online: sau thanh toán) |
| `shippedAt` | Thời gian xuất kho/giao hàng | ✅ Khi tạo đơn GHN (fix này) |
| `deliveredAt` | Thời gian giao thành công | Khi GHN webhook báo delivered |
| `ghnCreatedAt` | Thời gian tạo đơn GHN | Khi tạo đơn GHN |
| `ghnExpectedDeliveryTime` | Dự kiến giao hàng | Từ GHN API response |

## Hiển Thị Trên Frontend

### Warehouse Orders Page
Sau khi xuất kho, đơn hàng sẽ:
- Biến mất khỏi danh sách "Pending Export" (vì không còn status CONFIRMED)
- Có thể thêm tab "Đã xuất kho" để xem các đơn đã xuất

### Shipper Page
Đơn hàng sẽ xuất hiện trong danh sách của shipper với:
- Status: SHIPPING
- GHN Order Code: Hiển thị
- Có thể tracking

### Customer Order Page
Khách hàng sẽ thấy:
- Trạng thái: "Đang giao hàng"
- Mã vận đơn GHN
- Tracking information

## Testing

### 1. Tạo đơn hàng mới
```
POST /api/orders
→ Status: CONFIRMED (COD) hoặc PENDING_PAYMENT (Online)
```

### 2. Xuất kho
```
POST /api/inventory/export-for-sale
{
  "orderId": 1,
  "reason": "Xuất kho bán hàng",
  "items": [...]
}
→ Tạo phiếu xuất kho
→ Tạo đơn GHN
→ ✅ Cập nhật order.status = SHIPPING
→ ✅ Set order.shippedAt = now()
```

### 3. Kiểm tra order
```
GET /api/orders/1
→ status: "SHIPPING"
→ shippedAt: "2024-01-20T10:30:00"
→ ghnOrderCode: "GHN123456"
→ ghnShippingStatus: "created"
```

### 4. Kiểm tra warehouse orders
```
GET /api/inventory/orders/pending-export
→ Đơn hàng đã xuất không còn trong danh sách
```

## Trường Hợp Đặc Biệt

### 1. Giao hàng nội thành Hà Nội (Không dùng GHN)
```java
if (shippingService.isHanoiInnerCity(order.getProvince(), order.getDistrict())) {
    log.info("ℹ️ Order {} uses internal shipping, skip GHN", order.getOrderCode());
    return; // ❌ Không cập nhật status
}
```

**Vấn đề**: Đơn nội thành không tạo GHN → Không cập nhật status → Vẫn ở CONFIRMED

**Giải pháp**: Cần thêm logic cập nhật status cho đơn nội thành:

```java
if (shippingService.isHanoiInnerCity(order.getProvince(), order.getDistrict())) {
    log.info("ℹ️ Order {} uses internal shipping", order.getOrderCode());
    
    // ✅ Cập nhật status cho đơn nội thành
    order.setStatus(OrderStatus.SHIPPING);
    order.setShippedAt(LocalDateTime.now());
    orderRepository.save(order);
    
    return;
}
```

### 2. Miễn phí ship (shippingFee = 0)
Tương tự như trên, cần cập nhật status.

## Files Đã Thay Đổi

1. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/service/impl/InventoryServiceImpl.java`
   - Method: `createGHNOrderForExport()`
   - Thêm: `order.setStatus(OrderStatus.SHIPPING)`
   - Thêm: `order.setShippedAt(LocalDateTime.now())`

## TODO (Nếu cần)

- [ ] Cập nhật status cho đơn nội thành (không dùng GHN)
- [ ] Cập nhật status cho đơn miễn phí ship
- [ ] Thêm tab "Đã xuất kho" trong warehouse orders page
- [ ] Hiển thị timeline trạng thái đơn hàng cho khách
- [ ] Gửi email/notification khi đơn chuyển sang SHIPPING

## Lưu Ý

1. **Không hoàn tác**: Sau khi chuyển sang SHIPPING, không thể quay lại CONFIRMED
2. **GHN Webhook**: Khi GHN giao thành công, webhook sẽ cập nhật sang DELIVERED
3. **Accounting**: Cần kiểm tra xem có event listener nào lắng nghe status change không
4. **Notification**: Có thể gửi thông báo cho khách khi status = SHIPPING

## Troubleshooting

### Đơn hàng vẫn ở CONFIRMED sau xuất kho?
- Kiểm tra log xem có tạo GHN order thành công không
- Kiểm tra có exception nào trong `createGHNOrderForExport()` không
- Xem database: `ghnOrderCode` có được set không

### Đơn nội thành không chuyển status?
- Cần implement fix cho trường hợp đặc biệt (xem phần trên)

### Frontend không cập nhật?
- Refresh lại trang
- Kiểm tra API response có đúng status không
- Clear cache nếu cần
