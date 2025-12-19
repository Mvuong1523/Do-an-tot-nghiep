# Hướng dẫn Fix hiển thị tên Phường/Xã

## Vấn đề
Ở phần tạo đơn xuất kho bán hàng, tên xã không hiển thị mà cứ hiển thị mã ward code.

## Nguyên nhân
- Đơn hàng cũ chỉ lưu `ward` (mã code từ GHN), chưa có `wardName` (tên hiển thị)
- Đơn hàng mới đã được fix và sẽ lưu cả `wardName`

## Giải pháp

### Bước 1: Fix dữ liệu cũ (Backend)
Chạy API để cập nhật `wardName` cho tất cả đơn hàng cũ:

```http
POST http://localhost:8080/api/shipping/fix-ward-names
Authorization: Bearer <ADMIN_TOKEN>
```

API này sẽ:
- Tìm tất cả đơn hàng có `ward` code nhưng chưa có `wardName`
- Gọi GHN API để lấy tên phường/xã tương ứng
- Cập nhật `wardName` vào database
- Rebuild lại `shippingAddress` với tên phường/xã đúng (thay vì mã)

### Bước 2: Kiểm tra kết quả
Sau khi chạy API fix, kiểm tra lại đơn hàng:

```http
GET http://localhost:8080/api/inventory/orders/1
Authorization: Bearer <ADMIN_TOKEN>
```

Response sẽ có:
```json
{
  "ward": "20308",
  "wardName": "Phường Yên Hòa"  // ✅ Đã có tên
}
```

### Bước 3: Frontend đã được fix
File `src/frontend/app/warehouse/orders/[id]/page.tsx` đã được cập nhật để:
- Ưu tiên hiển thị `wardName` nếu có
- Hiển thị cảnh báo nếu chỉ có `ward` code
- Tự động fetch ward name từ GHN nếu cần

## Cách sử dụng

### Lấy Admin Token
1. Đăng nhập với tài khoản admin
2. Mở DevTools (F12) → Console
3. Chạy: `localStorage.getItem('token')`
4. Copy token

### Chạy Fix API
Sử dụng file `fix-ward-names.http` hoặc Postman:

```bash
# Thay <ADMIN_TOKEN> bằng token thực tế
curl -X POST http://localhost:8080/api/shipping/fix-ward-names \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Kết quả mong đợi
```json
{
  "success": true,
  "message": "Đã cập nhật tên phường/xã cho tất cả đơn hàng",
  "data": {
    "total": 10,
    "success": 10,
    "failed": 0
  }
}
```

## Đơn hàng mới
Từ bây giờ, tất cả đơn hàng mới sẽ tự động lưu `wardName` khi khách hàng chọn phường/xã trong checkout page.

## Kiểm tra
1. Vào trang warehouse orders: http://localhost:3000/warehouse/orders
2. Click vào một đơn hàng
3. Kiểm tra phần "Địa chỉ giao hàng chi tiết"
4. Phường/Xã sẽ hiển thị tên đầy đủ thay vì mã code

## Lưu ý
- API fix chỉ cần chạy 1 lần
- Nếu có đơn hàng mới vẫn bị thiếu `wardName`, kiểm tra lại checkout page
- Ward code vẫn được giữ để tích hợp với GHN API
