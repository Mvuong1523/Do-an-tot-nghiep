# Fix Hiển Thị Trạng Thái Đơn Hàng Cho Khách Hàng

## Vấn đề
Khi đơn hàng có status = `SHIPPING` (sau khi xuất kho), khách hàng thấy:
- ❌ "Đang giao hàng" (chưa chính xác, vì tài xế chưa lấy hàng)

## Yêu cầu
Hiển thị chính xác hơn:
- ✅ "Đã chuẩn bị hàng - Đợi tài xế đến lấy"

## Giải pháp

### 1. Order Detail Page (`/orders/[id]`)

**File**: `src/frontend/app/orders/[id]/page.tsx`

**Trước:**
```typescript
case 'SHIPPING':
  return 'Đang giao hàng'
```

**Sau:**
```typescript
case 'SHIPPING':
  return 'Đã chuẩn bị hàng - Đợi tài xế đến lấy'
```

### 2. Orders List Page (`/orders`)

**File**: `src/frontend/app/orders/page.tsx`

**Trước:**
```typescript
case 'shipping':
  return 'Đang giao hàng'
```

**Sau:**
```typescript
case 'shipping':
  return 'Đã chuẩn bị hàng - Đợi tài xế'
```

## Timeline Trạng Thái Đơn Hàng

### Từ góc nhìn khách hàng:

```
1. Chờ thanh toán (PENDING_PAYMENT)
   ↓ [Thanh toán thành công]
   
2. Đã xác nhận - Đang chuẩn bị hàng (CONFIRMED)
   ↓ [Nhân viên kho xuất kho]
   
3. Đã chuẩn bị hàng - Đợi tài xế đến lấy (SHIPPING) ← ✅ Fix này
   ↓ [Tài xế lấy hàng và giao]
   
4. Đang giao hàng (SHIPPING - sau khi tài xế lấy)
   ↓ [Giao thành công]
   
5. Đã giao hàng (DELIVERED)
   ↓
   
6. Hoàn thành (COMPLETED)
```

## Chi Tiết Các Trạng Thái

| Status | Text hiển thị | Ý nghĩa |
|--------|---------------|---------|
| `PENDING_PAYMENT` | Chờ thanh toán | Đơn mới tạo, chưa thanh toán |
| `CONFIRMED` | Đã xác nhận - Đang chuẩn bị hàng | Đã thanh toán, chờ kho xuất |
| `SHIPPING` | **Đã chuẩn bị hàng - Đợi tài xế đến lấy** | Đã xuất kho, chờ tài xế |
| `DELIVERED` | Đã giao hàng | Tài xế đã giao thành công |
| `COMPLETED` | Hoàn thành | Đơn hàng hoàn tất |
| `CANCELLED` | Đã hủy | Đơn bị hủy |

## Lý Do Thay Đổi

### Trước đây:
```
CONFIRMED → "Đã xác nhận - Đang chuẩn bị hàng"
SHIPPING  → "Đang giao hàng"
```

**Vấn đề**: 
- Khi vừa xuất kho xong, status = SHIPPING
- Nhưng tài xế chưa lấy hàng
- Hiển thị "Đang giao hàng" → Sai sự thật

### Bây giờ:
```
CONFIRMED → "Đã xác nhận - Đang chuẩn bị hàng"
SHIPPING  → "Đã chuẩn bị hàng - Đợi tài xế đến lấy"
```

**Chính xác hơn**:
- Khách biết hàng đã sẵn sàng
- Đang chờ tài xế đến lấy
- Minh bạch hơn về tiến trình

## Hiển Thị Trên UI

### Order Detail Page

**Header:**
```
┌─────────────────────────────────────┐
│ Đơn hàng #ORD20231119001           │
│                                     │
│ [Đã chuẩn bị hàng - Đợi tài xế]   │ ← Badge màu cam/xanh
└─────────────────────────────────────┘
```

**Timeline:**
```
✓ Đã thanh toán
✓ Đã xác nhận
✓ Đã chuẩn bị hàng
→ Đợi tài xế đến lấy hàng ← Current
○ Đang giao hàng
○ Đã giao hàng
```

### Orders List Page

**Table:**
```
| Mã đơn | Ngày đặt | Tổng tiền | Trạng thái |
|--------|----------|-----------|------------|
| ORD001 | 19/11    | 1,000,000 | [Đã chuẩn bị hàng - Đợi tài xế] |
```

## Màu Sắc Badge

Có thể giữ nguyên hoặc điều chỉnh:

```typescript
case 'shipping':
  return 'bg-orange-100 text-orange-800' // Màu cam - Đang chờ
  // hoặc
  return 'bg-blue-100 text-blue-800'     // Màu xanh - Đang xử lý
```

## Testing

### Test Case 1: Xem đơn hàng đã xuất kho
```
1. Đăng nhập với tài khoản khách hàng
2. Vào /orders
3. Click vào đơn hàng có status = SHIPPING
4. Kiểm tra:
   ✓ Header hiển thị "Đã chuẩn bị hàng - Đợi tài xế đến lấy"
   ✓ Timeline hiển thị đúng bước hiện tại
   ✓ Có thông tin mã vận đơn GHN (nếu có)
```

### Test Case 2: Danh sách đơn hàng
```
1. Vào /orders
2. Tìm đơn hàng có status = SHIPPING
3. Kiểm tra:
   ✓ Cột "Trạng thái" hiển thị "Đã chuẩn bị hàng - Đợi tài xế"
   ✓ Badge có màu phù hợp
```

### Test Case 3: So sánh với các status khác
```
CONFIRMED: "Đã xác nhận - Đang chuẩn bị hàng"
SHIPPING:  "Đã chuẩn bị hàng - Đợi tài xế đến lấy" ← Khác biệt rõ ràng
DELIVERED: "Đã giao hàng"
```

## Lưu Ý

1. **Không thay đổi backend**: Chỉ thay đổi text hiển thị trên frontend
2. **Status vẫn là SHIPPING**: Không tạo status mới
3. **Tương thích**: Các API khác vẫn hoạt động bình thường
4. **GHN Webhook**: Khi GHN cập nhật trạng thái, có thể cần thêm logic để phân biệt:
   - "Đợi tài xế lấy" (vừa tạo đơn)
   - "Đang giao hàng" (tài xế đã lấy)

## Cải Tiến Tương Lai (Optional)

### Phân biệt chi tiết hơn:

Có thể thêm sub-status hoặc dùng `ghnShippingStatus`:

```typescript
const getDetailedStatusText = (order) => {
  if (order.status === 'SHIPPING') {
    if (order.ghnShippingStatus === 'created' || order.ghnShippingStatus === 'ready_to_pick') {
      return 'Đã chuẩn bị hàng - Đợi tài xế đến lấy'
    } else if (order.ghnShippingStatus === 'picking' || order.ghnShippingStatus === 'delivering') {
      return 'Đang giao hàng'
    }
  }
  return getStatusText(order.status)
}
```

## Files Đã Thay Đổi

1. ✅ `src/frontend/app/orders/[id]/page.tsx`
   - Function: `getStatusText()`
   - Case: `SHIPPING`

2. ✅ `src/frontend/app/orders/page.tsx`
   - Function: `getStatusText()`
   - Case: `shipping`

## Kết Quả

Khách hàng bây giờ thấy trạng thái chính xác hơn:
- ✅ Biết hàng đã sẵn sàng
- ✅ Hiểu đang chờ tài xế
- ✅ Không nhầm lẫn với "đang giao hàng"
- ✅ Trải nghiệm minh bạch hơn
