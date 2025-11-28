# Cập nhật Frontend - Order Status

## ✅ Đã cập nhật các file:

### 1. `/orders/page.tsx` - Danh sách đơn hàng (Customer)
**Thay đổi:**
- ✅ Thêm `PENDING_PAYMENT` status
- ✅ Cập nhật màu sắc và icon
- ✅ Cập nhật filter tabs

**Status hiển thị:**
- `PENDING_PAYMENT` → "Chờ thanh toán" (cam)
- `PENDING` → "Chờ xác nhận" (vàng)
- `CONFIRMED` → "Đã xác nhận" (xanh)
- `SHIPPING` → "Đang giao hàng" (tím)
- `DELIVERED` → "Đã giao hàng" (xanh lá)
- `CANCELLED` → "Đã hủy" (đỏ)

**Filter tabs:**
- Tất cả
- Chờ thanh toán
- Đã xác nhận
- Đang giao
- Đã giao
- Đã hủy

### 2. `/orders/[id]/page.tsx` - Chi tiết đơn hàng (Customer)
**Thay đổi:**
- ✅ Thêm `PENDING_PAYMENT` status
- ✅ Cập nhật text: "Đã xác nhận - Đang chuẩn bị hàng"
- ✅ Nút "Tiếp tục thanh toán" chỉ hiện khi `PENDING_PAYMENT`
- ✅ Thông báo cảnh báo chỉ hiện khi `PENDING_PAYMENT`

### 3. `/sales/orders/page.tsx` - Quản lý đơn hàng (Sales Staff)
**Thay đổi:**
- ✅ Thêm `PENDING_PAYMENT` status
- ✅ Cập nhật màu sắc và icon
- ✅ Cập nhật filter tabs

**Filter tabs:**
- Tất cả
- Chờ thanh toán
- Đã xác nhận
- Đang giao
- Đã giao
- Đã hủy

### 4. `/payment/[orderCode]/page.tsx` - Trang thanh toán
**Đã có sẵn:**
- ✅ Polling mỗi 5 giây
- ✅ Tự động redirect khi thanh toán thành công
- ✅ Nút "Hủy đơn hàng"
- ✅ Countdown timer

## 🎨 Màu sắc Status

| Status | Màu | Class |
|--------|-----|-------|
| PENDING_PAYMENT | Cam | `bg-orange-100 text-orange-800` |
| PENDING | Vàng | `bg-yellow-100 text-yellow-800` |
| CONFIRMED | Xanh dương | `bg-blue-100 text-blue-800` |
| SHIPPING | Tím | `bg-purple-100 text-purple-800` |
| DELIVERED | Xanh lá | `bg-green-100 text-green-800` |
| CANCELLED | Đỏ | `bg-red-100 text-red-800` |

## 🔄 Flow UI

### Customer View

**1. Đặt hàng COD:**
```
Checkout → Đặt hàng → Redirect /orders → Status: "Đã xác nhận"
```

**2. Đặt hàng Online:**
```
Checkout → Đặt hàng → Redirect /payment/ORD... → Status: "Chờ thanh toán"
                                    ↓
                            Thanh toán thành công
                                    ↓
                        Redirect /orders/ORD...?success=true
                                    ↓
                            Status: "Đã xác nhận"
```

**3. Thoát ra khi đang thanh toán:**
```
/payment/ORD... → Thoát → /orders → Thấy đơn "Chờ thanh toán"
                                           ↓
                                  Click "Tiếp tục thanh toán"
                                           ↓
                                  Quay lại /payment/ORD...
```

**4. Hủy đơn:**
```
/payment/ORD... → Nhấn "Hủy đơn hàng" → Confirm → Status: "Đã hủy"
```

### Sales Staff View

**1. Xem danh sách đơn:**
```
/sales/orders → Filter theo status → Xem chi tiết
```

**2. Xử lý đơn:**
```
PENDING_PAYMENT → Chờ khách thanh toán (không thể xử lý)
CONFIRMED → Xác nhận → SHIPPING → DELIVERED
```

## 📱 Responsive

Tất cả các trang đều responsive:
- ✅ Mobile: Stack vertical
- ✅ Tablet: 2 columns
- ✅ Desktop: Full layout

## 🧪 Test Checklist

### Customer
- [ ] Đặt hàng COD → Thấy "Đã xác nhận"
- [ ] Đặt hàng Online → Thấy "Chờ thanh toán"
- [ ] Thanh toán thành công → Thấy "Đã xác nhận"
- [ ] Thoát ra → Vẫn thấy "Chờ thanh toán"
- [ ] Click "Tiếp tục thanh toán" → Quay lại trang thanh toán
- [ ] Hủy đơn → Thấy "Đã hủy"
- [ ] Filter tabs hoạt động đúng

### Sales Staff
- [ ] Xem danh sách đơn hàng
- [ ] Filter theo status
- [ ] Thấy đơn "Chờ thanh toán"
- [ ] Thấy đơn "Đã xác nhận"
- [ ] Xử lý đơn hàng

## 🎯 Điểm khác biệt

### Trước:
- Chỉ có PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
- Không phân biệt đơn chờ thanh toán

### Sau:
- Thêm PENDING_PAYMENT
- Phân biệt rõ: Chờ thanh toán vs Đã xác nhận
- UX tốt hơn với nút "Tiếp tục thanh toán"

Happy coding! 🎉
