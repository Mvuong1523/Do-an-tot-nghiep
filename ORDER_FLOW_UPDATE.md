# Cập nhật luồng đặt hàng

## Luồng mới (Đã cập nhật)

### 1. Khách hàng đặt hàng
- Khách hàng chọn sản phẩm, điền thông tin giao hàng
- Bấm "Đặt hàng"
- **Đơn hàng tự động chuyển sang trạng thái CONFIRMED** (Đã xác nhận)
- ❌ Không cần nhân viên duyệt đơn nữa

### 2. Trạng thái đơn hàng

```
CONFIRMED (Đã xác nhận) 
    ↓
SHIPPING (Tài xế đang giao)
    ↓
DELIVERED (Đã giao thành công)
```

### 3. Hủy đơn hàng

**Khách hàng có thể hủy:**
- ✅ Khi đơn ở trạng thái CONFIRMED
- ✅ Khi đơn ở trạng thái SHIPPING (đang giao)
- ❌ KHÔNG thể hủy khi đã DELIVERED

**Hoàn tiền:**
- Nếu đã thanh toán online → Hoàn tiền tự động trong 3-5 ngày
- Nếu COD → Không cần hoàn tiền

### 4. Quyền hạn

**Khách hàng:**
- Đặt hàng (tự động xác nhận)
- Hủy đơn (khi chưa giao)
- Xem lịch sử đơn hàng

**Nhân viên Sales/Admin:**
- Xem tất cả đơn hàng
- Cập nhật trạng thái: SHIPPING, DELIVERED
- Hủy đơn (bất kỳ lúc nào trừ DELIVERED)

**Tài xế (Shipper):**
- Nhận đơn hàng CONFIRMED
- Cập nhật trạng thái SHIPPING (đang giao)
- Cập nhật trạng thái DELIVERED (đã giao)

## Thay đổi trong code

### Backend

**OrderServiceImpl.java:**
```java
// Tự động xác nhận khi tạo đơn
.status(OrderStatus.CONFIRMED)  // Thay vì PENDING
.confirmedAt(LocalDateTime.now())

// Cho phép hủy đơn khi CONFIRMED hoặc SHIPPING
if (order.getStatus() == OrderStatus.DELIVERED) {
    return ApiResponse.error("Không thể hủy đơn hàng đã giao thành công");
}
```

### Frontend

**Cần cập nhật:**
1. Xóa tab "Chờ xác nhận" trong giao diện khách hàng
2. Đổi tên trạng thái hiển thị
3. Ẩn nút "Xác nhận đơn" của nhân viên
4. Chỉ hiển thị nút "Cập nhật trạng thái giao hàng"

## API Endpoints

### Khách hàng
- `POST /api/orders` - Tạo đơn (tự động CONFIRMED)
- `PUT /api/orders/{id}/cancel` - Hủy đơn
- `GET /api/orders` - Xem đơn hàng của mình

### Admin/Sales
- `GET /api/admin/orders` - Xem tất cả đơn
- `PUT /api/admin/orders/{id}/shipping` - Cập nhật đang giao
- `PUT /api/admin/orders/{id}/delivered` - Cập nhật đã giao
- `PUT /api/admin/orders/{id}/cancel` - Hủy đơn

## Lưu ý

1. ✅ Đơn hàng tự động xác nhận ngay khi tạo
2. ✅ Khách hàng có thể hủy đến khi chưa giao
3. ✅ Nếu đã thanh toán, cần xử lý hoàn tiền
4. ⚠️ Cần tích hợp API hoàn tiền (TODO)
5. ⚠️ Cần thông báo cho khách khi đơn chuyển trạng thái
