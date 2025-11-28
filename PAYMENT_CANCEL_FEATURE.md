# Tính năng Hủy đơn hàng khi thanh toán

## ✅ Đã hoàn thành

### 1. Trang thanh toán (`/payment/[orderCode]`)

**Thêm nút "Hủy đơn hàng":**
- Nút màu đỏ nổi bật ở cuối trang
- Có confirm dialog trước khi hủy
- Hiển thị loading state khi đang xử lý
- Tự động dừng polling khi hủy
- Redirect về trang danh sách đơn hàng sau khi hủy thành công

**Logic hủy đơn:**
```typescript
const handleCancelOrder = async () => {
  // 1. Confirm với user
  // 2. Dừng polling payment status
  // 3. Gọi API hủy đơn: POST /api/orders/{orderId}/cancel
  // 4. Redirect về /orders
}
```

### 2. Trang chi tiết đơn hàng (`/orders/[id]`)

**Thêm nút "Tiếp tục thanh toán":**
- Hiển thị khi: `status === 'PENDING'` VÀ `paymentStatus === 'UNPAID' hoặc 'PENDING'`
- Nút màu xanh dương, nổi bật
- Link đến `/payment/{orderCode}`

**Thêm thông báo cảnh báo:**
- Box màu vàng cảnh báo đơn hàng đang chờ thanh toán
- Hướng dẫn user nhấn nút "Tiếp tục thanh toán"

### 3. Sửa lỗi authentication

**Vấn đề:** Khi refresh trang thanh toán, bị redirect về login
**Nguyên nhân:** Zustand store chưa kịp hydrate từ localStorage
**Giải pháp:** Check cả `isAuthenticated` VÀ `localStorage.getItem('auth_token')`

```typescript
const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null

if (!isAuthenticated && !token) {
  // Chỉ redirect khi cả 2 đều không có
  router.push('/login')
}
```

## 🎯 Flow hoạt động

### Kịch bản 1: Khách hàng hủy trong quá trình thanh toán

1. Khách vào trang thanh toán: `/payment/ORD202511260193`
2. Thấy QR code và thông tin chuyển khoản
3. Quyết định không muốn mua → Nhấn "Hủy đơn hàng"
4. Confirm dialog xuất hiện
5. Nhấn OK → Đơn hàng bị hủy
6. Redirect về `/orders`

### Kịch bản 2: Khách hàng thoát ra rồi quay lại

1. Khách vào trang thanh toán
2. Thoát ra (đóng tab, back, v.v.)
3. Đơn hàng vẫn ở trạng thái PENDING, chờ thanh toán
4. Khách vào lại `/orders/ORD202511260193`
5. Thấy nút "Tiếp tục thanh toán" và thông báo cảnh báo
6. Nhấn nút → Quay lại trang thanh toán
7. Có thể tiếp tục thanh toán hoặc hủy đơn

### Kịch bản 3: Hết thời gian thanh toán

1. Khách vào trang thanh toán
2. Không thanh toán trong 15 phút
3. Timer hết → Tự động redirect về trang đơn hàng
4. Backend có scheduled job tự động hủy đơn hết hạn

## 📝 API Endpoints sử dụng

### Hủy đơn hàng
```http
POST /api/orders/{orderId}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Khách hàng hủy trong quá trình thanh toán"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã hủy đơn hàng"
}
```

### Kiểm tra trạng thái thanh toán
```http
GET /api/payment/{paymentCode}/status
```

**Response:**
```json
{
  "success": true,
  "data": {
    "paymentCode": "PAY202511263145",
    "status": "SUCCESS",
    "amount": 30007.0
  }
}
```

## 🧪 Test Cases

### Test 1: Hủy đơn thành công
1. Tạo đơn hàng mới
2. Vào trang thanh toán
3. Nhấn "Hủy đơn hàng"
4. Confirm
5. ✅ Đơn hàng bị hủy
6. ✅ Redirect về /orders
7. ✅ Đơn hàng có status = CANCELLED

### Test 2: Tiếp tục thanh toán
1. Tạo đơn hàng mới
2. Vào trang thanh toán
3. Thoát ra
4. Vào trang chi tiết đơn hàng
5. ✅ Thấy nút "Tiếp tục thanh toán"
6. ✅ Thấy thông báo cảnh báo
7. Nhấn nút
8. ✅ Quay lại trang thanh toán

### Test 3: Refresh không bị logout
1. Vào trang thanh toán
2. Refresh (F5)
3. ✅ Không bị redirect về login
4. ✅ Trang load bình thường

### Test 4: Thanh toán thành công
1. Vào trang thanh toán
2. Gọi test webhook: `http://localhost:8080/api/payment/test-webhook/{paymentCode}`
3. ✅ Polling phát hiện SUCCESS
4. ✅ Toast "Thanh toán thành công!"
5. ✅ Redirect về `/orders/{orderCode}?success=true`

## 🎨 UI/UX Improvements

### Trang thanh toán
- ✅ Nút hủy màu đỏ, dễ nhận biết
- ✅ Confirm dialog tránh hủy nhầm
- ✅ Loading state khi đang xử lý
- ✅ Text hướng dẫn rõ ràng

### Trang đơn hàng
- ✅ Nút "Tiếp tục thanh toán" nổi bật
- ✅ Thông báo cảnh báo màu vàng
- ✅ Hướng dẫn user hành động tiếp theo

## 🔧 Technical Details

### State Management
```typescript
const [cancelling, setCancelling] = useState(false)
```

### Polling Control
```typescript
// Dừng polling khi hủy đơn
if (pollingInterval.current) {
  clearInterval(pollingInterval.current)
}
```

### Error Handling
```typescript
try {
  // Cancel order
} catch (error) {
  toast.error('Lỗi khi hủy đơn hàng')
} finally {
  setCancelling(false)
}
```

## 📱 Responsive Design

- ✅ Mobile: Nút xếp dọc
- ✅ Desktop: Nút xếp ngang
- ✅ Tablet: Tự động điều chỉnh

## 🚀 Next Steps (Optional)

1. **Email notification** khi đơn bị hủy
2. **Lý do hủy** cho phép user nhập
3. **Thống kê** tỷ lệ hủy đơn
4. **Retry payment** với payment code mới
5. **Push notification** khi thanh toán thành công

## ✅ Checklist

- [x] Thêm nút "Hủy đơn hàng" trong trang thanh toán
- [x] Implement logic hủy đơn
- [x] Thêm nút "Tiếp tục thanh toán" trong trang đơn hàng
- [x] Thêm thông báo cảnh báo
- [x] Sửa lỗi authentication khi refresh
- [x] Test flow hủy đơn
- [x] Test flow tiếp tục thanh toán
- [x] Responsive design
- [x] Error handling
- [x] Loading states

Happy coding! 🎉
