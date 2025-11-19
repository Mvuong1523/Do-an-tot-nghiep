# 🧪 HƯỚNG DẪN TEST HỆ THỐNG

## 📋 Chuẩn bị

### 1. Khởi động Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```
Hoặc chạy từ IDE (IntelliJ/Eclipse):
- Run `WebTMDTApplication.java`
- Server sẽ chạy tại: `http://localhost:8080`

### 2. Khởi động Frontend (Next.js)
```bash
cd src/frontend
npm install  # Lần đầu tiên
npm run dev
```
- Frontend sẽ chạy tại: `http://localhost:3000`

---

## 🔐 Test Authentication

### 1. Đăng ký tài khoản
- Truy cập: `http://localhost:3000/register`
- Điền thông tin:
  - Email: `test@example.com`
  - Password: `123456`
  - Full Name: `Nguyen Van A`
- Click "Đăng ký"

### 2. Đăng nhập
- Truy cập: `http://localhost:3000/login`
- Nhập email/password vừa đăng ký
- Click "Đăng nhập"
- Kiểm tra: Header hiển thị tên user

---

## 🛍️ Test Flow Mua Hàng (Customer)

### Bước 1: Xem sản phẩm
1. Truy cập trang chủ: `http://localhost:3000`
2. Kiểm tra:
   - ✅ Hiển thị danh sách sản phẩm
   - ✅ Sidebar danh mục
   - ✅ Search bar
   - ✅ Filter theo danh mục

### Bước 2: Chi tiết sản phẩm
1. Click vào 1 sản phẩm
2. Kiểm tra:
   - ✅ Hiển thị hình ảnh, giá, mô tả
   - ✅ Chọn số lượng
   - ✅ Nút "Thêm vào giỏ"
   - ✅ Nút "Mua ngay"

### Bước 3: Thêm vào giỏ hàng
1. Click "Thêm vào giỏ hàng"
2. Kiểm tra:
   - ✅ Toast "Đã thêm vào giỏ hàng"
   - ✅ Icon giỏ hàng có số lượng

### Bước 4: Xem giỏ hàng
1. Click icon giỏ hàng hoặc truy cập: `http://localhost:3000/cart`
2. Kiểm tra:
   - ✅ Hiển thị danh sách sản phẩm
   - ✅ Cập nhật số lượng (+/-)
   - ✅ Xóa sản phẩm
   - ✅ Tính tổng tiền tự động
   - ✅ Phí ship (tạm thời = 0)

### Bước 5: Checkout
1. Click "Tiến hành thanh toán"
2. Điền thông tin:
   - Họ tên: `Nguyen Van A`
   - SĐT: `0912345678`
   - Email: `test@example.com`
   - Tỉnh: `Hà Nội`
   - Quận: `Cầu Giấy` (nội thành - miễn phí ship)
   - Phường: `Dịch Vọng`
   - Địa chỉ: `123 Đường ABC`
3. Kiểm tra:
   - ✅ Phí ship = 0đ (nội thành HN)
   - ✅ Tổng tiền = Tạm tính + Phí ship
4. Thử đổi quận:
   - Quận: `Sóc Sơn` (ngoại thành)
   - ✅ Phí ship = 25.000đ
5. Click "Đặt hàng"

### Bước 6: Thanh toán
1. Tự động chuyển đến trang thanh toán
2. Kiểm tra:
   - ✅ Hiển thị QR Code
   - ✅ Thông tin chuyển khoản
   - ✅ Countdown 15 phút
   - ✅ Nút copy số TK, nội dung
   - ✅ Auto polling check payment (mỗi 3s)
3. Chờ ~30s để demo auto success
4. Kiểm tra:
   - ✅ Toast "Thanh toán thành công"
   - ✅ Redirect đến trang đơn hàng

---

## 📦 Test Quản Lý Kho (WAREHOUSE)

### Chuẩn bị:
- Cần tài khoản WAREHOUSE role
- Hoặc dùng ADMIN

### 1. Xem tồn kho
- Truy cập: `http://localhost:3000/admin/inventory`
- Tab "Tồn kho"
- Kiểm tra:
  - ✅ Hiển thị data từ DB (không phải mock)
  - ✅ Các cột: SKU, Tên, NCC, Tồn kho, Đã giữ, Có thể bán

### 2. Tạo phiếu nhập
- Tab "Phiếu xuất nhập"
- Click "Nhập hàng"
- Điền thông tin:
  - Mã phiếu: `PO20231119001`
  - Nhà cung cấp: Tạo mới hoặc chọn có sẵn
  - Thêm sản phẩm với SKU, số lượng, giá
- Click "Tạo phiếu"
- Kiểm tra:
  - ✅ Toast success
  - ✅ Phiếu xuất hiện trong danh sách

### 3. Hoàn tất nhập kho
- Click "Chi tiết" phiếu vừa tạo
- Click "Hoàn tất nhập kho"
- Nhập serial cho từng sản phẩm
- Click "Xác nhận"
- Kiểm tra:
  - ✅ Trạng thái = "Đã nhập"
  - ✅ Tồn kho tăng

### 4. Xuất kho
- Click "Xuất hàng"
- Chọn sản phẩm và nhập serial
- Click "Xuất kho"
- Kiểm tra:
  - ✅ Tồn kho giảm
  - ✅ Serial status = SOLD

---

## 🏷️ Test Quản Lý Sản Phẩm (PRODUCT_MANAGER)

### 1. Xem sản phẩm trong kho
- Truy cập: `http://localhost:3000/admin/products/publish`
- Kiểm tra:
  - ✅ Danh sách sản phẩm trong kho
  - ✅ Trạng thái: Đã/Chưa đăng bán
  - ✅ Số lượng tồn kho

### 2. Đăng bán sản phẩm
- Click "Đăng bán" trên sản phẩm chưa đăng
- Điền thông tin:
  - Tên hiển thị
  - Danh mục
  - Giá bán
  - Mô tả
  - URL hình ảnh
- Click "Đăng bán"
- Kiểm tra:
  - ✅ Trạng thái = "Đã đăng bán"
  - ✅ Sản phẩm xuất hiện trên trang chủ

### 3. Gỡ sản phẩm
- Click icon "Xóa" trên sản phẩm đã đăng
- Confirm
- Kiểm tra:
  - ✅ Trạng thái = "Chưa đăng bán"
  - ✅ Sản phẩm biến mất khỏi trang chủ

---

## 🔧 Test API với Postman/Thunder Client

### 1. Login để lấy token
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456"
}
```
Response:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "user": {...}
  }
}
```

### 2. Test Cart API
```http
GET http://localhost:8080/api/cart
Authorization: Bearer {token}
```

```http
POST http://localhost:8080/api/cart/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

### 3. Test Order API
```http
POST http://localhost:8080/api/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "customerName": "Nguyen Van A",
  "customerPhone": "0912345678",
  "customerEmail": "test@example.com",
  "province": "Hà Nội",
  "district": "Cầu Giấy",
  "ward": "Dịch Vọng",
  "address": "123 Đường ABC",
  "shippingFee": 0
}
```

### 4. Test Payment API
```http
POST http://localhost:8080/api/payment/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 1,
  "amount": 1000000
}
```

### 5. Test Webhook (Mock SePay)
```http
POST http://localhost:8080/api/payment/sepay/webhook
Content-Type: application/json

{
  "transactionId": "SEP123456",
  "bankCode": "VCB",
  "accountNumber": "1234567890",
  "amount": 1000000,
  "content": "PAY20231119001",
  "transactionDate": "2023-11-19T10:25:00",
  "status": "SUCCESS",
  "signature": "test_signature"
}
```

---

## 🐛 Các vấn đề thường gặp

### 1. Frontend không kết nối được Backend
- Kiểm tra Backend đang chạy: `http://localhost:8080`
- Kiểm tra CORS trong SecurityConfig
- Kiểm tra API_BASE_URL trong frontend

### 2. 403 Forbidden
- Kiểm tra token JWT có hợp lệ không
- Kiểm tra role của user
- Kiểm tra SecurityConfig

### 3. Giỏ hàng trống sau khi thêm
- Kiểm tra API cart có hoạt động không
- Kiểm tra localStorage có token không
- Kiểm tra console log

### 4. Thanh toán không tự động check
- Kiểm tra polling interval
- Kiểm tra API check status
- Kiểm tra webhook có được gọi không

---

## ✅ Checklist Test Đầy Đủ

### Customer Flow
- [ ] Đăng ký tài khoản
- [ ] Đăng nhập
- [ ] Xem danh sách sản phẩm
- [ ] Filter theo danh mục
- [ ] Xem chi tiết sản phẩm
- [ ] Thêm vào giỏ hàng
- [ ] Cập nhật số lượng trong giỏ
- [ ] Xóa sản phẩm khỏi giỏ
- [ ] Checkout với địa chỉ nội thành HN (free ship)
- [ ] Checkout với địa chỉ ngoại thành (có phí)
- [ ] Thanh toán với QR Code
- [ ] Auto check payment status
- [ ] Xem lịch sử đơn hàng

### Warehouse Flow
- [ ] Xem tồn kho (data từ DB)
- [ ] Tạo phiếu nhập
- [ ] Hoàn tất nhập kho (nhập serial)
- [ ] Tạo phiếu xuất
- [ ] Xem báo cáo

### Product Manager Flow
- [ ] Xem sản phẩm trong kho
- [ ] Đăng bán sản phẩm
- [ ] Chỉnh sửa thông tin
- [ ] Gỡ sản phẩm
- [ ] Quản lý danh mục

### Admin Flow
- [ ] Tất cả quyền của Warehouse
- [ ] Tất cả quyền của Product Manager
- [ ] Duyệt nhân viên
- [ ] Xóa sản phẩm

---

## 📊 Test Performance

### Load Test với Artillery
```bash
npm install -g artillery
artillery quick --count 10 --num 100 http://localhost:8080/api/products
```

### Monitor với Spring Boot Actuator
- Thêm dependency: `spring-boot-starter-actuator`
- Truy cập: `http://localhost:8080/actuator/health`

---

## 🎯 Next Steps

Sau khi test xong, cần làm:
1. Fix bugs phát hiện được
2. Tích hợp API thật (GHTK, SePay)
3. Thêm email notification
4. Thêm unit tests
5. Deploy lên server

---

*Happy Testing! 🚀*
