# Cập nhật Trạng thái Đơn hàng

## ✅ Đã hoàn thành

### 1. Thêm trạng thái mới: `PENDING_PAYMENT`

**File:** `OrderStatus.java`

```java
public enum OrderStatus {
    PENDING_PAYMENT, // Chờ thanh toán (đơn online)
    PENDING,         // Chờ xác nhận (sau khi thanh toán hoặc COD)
    CONFIRMED,       // Đã xác nhận
    PROCESSING,      // Đang xử lý
    SHIPPING,        // Đang giao hàng
    DELIVERED,       // Đã giao hàng
    COMPLETED,       // Hoàn thành
    CANCELLED,       // Đã hủy
    RETURNED         // Đã trả hàng
}
```

### 2. Logic tạo đơn hàng

**File:** `OrderServiceImpl.java`

**Thanh toán Online (SEPAY):**
```
Tạo đơn → PENDING_PAYMENT (chờ thanh toán)
```

**Thanh toán COD:**
```
Tạo đơn → PENDING (chờ xác nhận)
```

### 3. Logic thanh toán thành công

**File:** `PaymentServiceImpl.java`

**Khi webhook nhận thanh toán thành công:**
```
PENDING_PAYMENT → PENDING (chờ xác nhận)
```

### 4. Frontend hiển thị

**File:** `orders/[id]/page.tsx`

**Trạng thái:**
- `PENDING_PAYMENT` → "Chờ thanh toán" (màu cam)
- `PENDING` → "Chờ xác nhận" (màu vàng)
- `CONFIRMED` → "Đã xác nhận" (màu xanh)

**Nút "Tiếp tục thanh toán":**
- Chỉ hiển thị khi `status = PENDING_PAYMENT`

## 🔄 Flow hoàn chỉnh

### Flow 1: Đơn hàng Online (SEPAY)

```
1. Khách đặt hàng với SEPAY
   ↓
2. Tạo đơn: status = PENDING_PAYMENT
   ↓
3. Redirect đến trang thanh toán
   ↓
4. Khách thanh toán (quét QR)
   ↓
5. Webhook nhận thông báo
   ↓
6. Cập nhật: status = CONFIRMED (tự động xác nhận)
   ↓
7. Tiếp tục: SHIPPING → DELIVERED → COMPLETED
```

### Flow 2: Đơn hàng COD

```
1. Khách đặt hàng với COD
   ↓
2. Tạo đơn: status = CONFIRMED (tự động xác nhận)
   ↓
3. Tiếp tục: SHIPPING → DELIVERED → COMPLETED
```

### Flow 3: Hủy đơn khi chờ thanh toán

```
1. Đơn hàng: status = PENDING_PAYMENT
   ↓
2. Khách nhấn "Hủy đơn hàng"
   ↓
3. Cập nhật: status = CANCELLED
   ↓
4. Giải phóng stock đã reserve
```

### Flow 4: Hết hạn thanh toán

```
1. Đơn hàng: status = PENDING_PAYMENT
   ↓
2. Quá 15 phút không thanh toán
   ↓
3. Scheduled job tự động hủy
   ↓
4. Cập nhật: status = CANCELLED
   ↓
5. Giải phóng stock đã reserve
```

## 📊 Bảng trạng thái

| Status | Tên hiển thị | Màu sắc | Mô tả |
|--------|-------------|---------|-------|
| PENDING_PAYMENT | Chờ thanh toán | Cam | Đơn online chưa thanh toán |
| PENDING | Chờ xác nhận | Vàng | Đã thanh toán hoặc COD, chờ admin xác nhận |
| CONFIRMED | Đã xác nhận | Xanh dương | Admin đã xác nhận, chuẩn bị hàng |
| PROCESSING | Đang xử lý | Xanh dương | Đang chuẩn bị hàng |
| SHIPPING | Đang giao hàng | Tím | Đang vận chuyển |
| DELIVERED | Đã giao hàng | Xanh lá | Đã giao thành công |
| COMPLETED | Hoàn thành | Xanh lá | Hoàn tất giao dịch |
| CANCELLED | Đã hủy | Đỏ | Đơn bị hủy |
| RETURNED | Đã trả hàng | Đỏ | Khách trả hàng |

## 🎯 Điểm khác biệt chính

### Trước đây:
```
Online: PENDING → CONFIRMED (sau thanh toán)
COD:    CONFIRMED (ngay lập tức)
```

### Bây giờ:
```
Online: PENDING_PAYMENT → CONFIRMED (tự động sau thanh toán)
COD:    CONFIRMED (tự động ngay lập tức)
```

## ✅ Lợi ích

1. **Tự động hóa hoàn toàn:**
   - COD: Tự động xác nhận ngay khi đặt hàng
   - Online: Tự động xác nhận sau khi thanh toán thành công
   - Không cần admin xác nhận thủ công

2. **Phân biệt rõ ràng:**
   - `PENDING_PAYMENT`: Đang chờ khách thanh toán
   - `CONFIRMED`: Đã xác nhận, chờ chuẩn bị hàng

3. **UX tốt hơn:**
   - Khách biết đơn đang ở trạng thái nào
   - Nút "Tiếp tục thanh toán" chỉ hiện khi cần
   - Có thể thoát ra và quay lại thanh toán sau

4. **Quản lý linh hoạt:**
   - Đơn chờ thanh toán có thời gian đếm ngược
   - Có thể hủy đơn khi đang chờ thanh toán
   - Tự động hủy đơn hết hạn

## 🧪 Test Cases

### Test 1: Đặt hàng Online
1. Đặt hàng với SEPAY
2. ✅ Status = PENDING_PAYMENT
3. ✅ Hiển thị "Chờ thanh toán" (màu cam)
4. ✅ Có nút "Tiếp tục thanh toán"

### Test 2: Thanh toán thành công
1. Đơn hàng PENDING_PAYMENT
2. Thanh toán (test webhook)
3. ✅ Status = CONFIRMED (tự động)
4. ✅ Hiển thị "Đã xác nhận" (màu xanh)
5. ✅ Không còn nút "Tiếp tục thanh toán"

### Test 3: Đặt hàng COD
1. Đặt hàng với COD
2. ✅ Status = CONFIRMED (tự động)
3. ✅ Hiển thị "Đã xác nhận" (màu xanh)
4. ✅ Không có nút "Tiếp tục thanh toán"

### Test 4: Thoát ra khi đang thanh toán
1. Đơn hàng PENDING_PAYMENT
2. Thoát ra (không hủy)
3. ✅ Status vẫn = PENDING_PAYMENT
4. ✅ Có thể quay lại thanh toán
5. ✅ Thời gian đếm ngược vẫn chạy

### Test 5: Hủy đơn khi đang thanh toán
1. Đơn hàng PENDING_PAYMENT
2. Nhấn "Hủy đơn hàng"
3. ✅ Status = CANCELLED
4. ✅ Hiển thị "Đã hủy" (màu đỏ)
5. ✅ Giải phóng stock

## 📝 Notes

- Stock được reserve ngay khi tạo đơn (cả PENDING_PAYMENT và PENDING)
- Khi hủy đơn, stock được giải phóng
- Payment timeout: 15 phút
- Scheduled job chạy mỗi 5 phút để check đơn hết hạn

Happy coding! 🎉
