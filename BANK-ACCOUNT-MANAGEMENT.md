# 🏦 Hệ thống quản lý tài khoản ngân hàng

## ✅ Đã hoàn thành

### Backend
- ✅ Entity `BankAccount` - Lưu thông tin tài khoản ngân hàng
- ✅ Repository, Service, Controller - CRUD đầy đủ
- ✅ API quản lý tài khoản
- ✅ Tích hợp với SePay - Tự động lấy tài khoản mặc định

### Frontend
- ✅ Trang Admin `/admin/bank-accounts`
- ✅ Thêm/sửa/xóa tài khoản
- ✅ Đặt tài khoản mặc định
- ✅ Kích hoạt/tạm dừng tài khoản

---

## 🎯 Tính năng

### 1. Quản lý nhiều tài khoản
- Thêm nhiều tài khoản ngân hàng
- Mỗi tài khoản có: Ngân hàng, Số TK, Tên TK, Ghi chú
- Kích hoạt/tạm dừng tài khoản

### 2. Tài khoản mặc định
- Đặt 1 tài khoản làm mặc định
- Tài khoản mặc định sẽ được dùng để:
  - Tạo QR code thanh toán
  - Nhận tiền từ khách hàng
  - Hiển thị trên hóa đơn

### 3. Tích hợp SePay
- Khi khách thanh toán online → Dùng tài khoản mặc định
- Tự động tạo QR code với thông tin tài khoản
- Nếu không có tài khoản mặc định → Dùng config cũ

---

## 📊 Database Schema

```sql
CREATE TABLE bank_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_code VARCHAR(50) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

---

## 🔧 API Endpoints

### Admin APIs (Chỉ ADMIN)

```
GET    /api/admin/bank-accounts              # Lấy danh sách
GET    /api/admin/bank-accounts/{id}         # Lấy chi tiết
GET    /api/admin/bank-accounts/default      # Lấy tài khoản mặc định
POST   /api/admin/bank-accounts              # Thêm mới
PUT    /api/admin/bank-accounts/{id}         # Cập nhật
DELETE /api/admin/bank-accounts/{id}         # Xóa
PUT    /api/admin/bank-accounts/{id}/set-default      # Đặt mặc định
PUT    /api/admin/bank-accounts/{id}/toggle-active   # Bật/tắt
```

---

## 🚀 Cách sử dụng

### 1. Vào trang quản lý
```
http://localhost:3000/admin/bank-accounts
```

### 2. Thêm tài khoản mới

Click **"Thêm tài khoản"**

Điền thông tin:
```
Ngân hàng: MBBank
Số tài khoản: 3333315012003
Tên tài khoản: LE MINH VUONG
Ghi chú: Tài khoản chính
☑ Kích hoạt
☑ Đặt làm mặc định
```

Click **"Thêm"**

### 3. Quản lý tài khoản

#### Đặt làm mặc định (⭐)
- Click icon ⭐ trên tài khoản
- Tài khoản này sẽ được dùng cho thanh toán

#### Kích hoạt/Tạm dừng (✅/❌)
- Click icon ✅ để tạm dừng
- Click icon ❌ để kích hoạt lại

#### Sửa (✏️)
- Click icon ✏️
- Cập nhật thông tin
- Click "Cập nhật"

#### Xóa (🗑️)
- Click icon 🗑️
- Xác nhận xóa
- **Lưu ý:** Không thể xóa tài khoản mặc định

---

## 💰 Luồng thanh toán

### Trước khi có hệ thống:
```
Khách thanh toán
    ↓
Dùng tài khoản cố định trong application.properties
    ↓
Tiền vào tài khoản: 3333315012003 - LE MINH VUONG
```

### Sau khi có hệ thống:
```
Khách thanh toán
    ↓
Lấy tài khoản mặc định từ database
    ↓
Tạo QR code với tài khoản đó
    ↓
Tiền vào tài khoản mặc định
```

---

## 🎨 Giao diện

### Desktop View
```
┌────────────────────────────────────────────────────┐
│  Quản lý tài khoản ngân hàng    [+ Thêm tài khoản] │
├────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────┐  │
│  │ MB Bank                [⭐ Mặc định] [Đang dùng] │
│  │ Số TK: 3333315012003                         │  │
│  │ Tên TK: LE MINH VUONG                        │  │
│  │ Ghi chú: Tài khoản chính                     │  │
│  │                                               │  │
│  │                    [⭐] [✅] [✏️] [🗑️]        │  │
│  └──────────────────────────────────────────────┘  │
│                                                    │
│  ┌──────────────────────────────────────────────┐  │
│  │ Vietcombank                      [Tạm dừng]  │  │
│  │ Số TK: 1234567890                            │  │
│  │ Tên TK: NGUYEN VAN A                         │  │
│  │                                               │  │
│  │                    [⭐] [❌] [✏️] [🗑️]        │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### Modal thêm/sửa
```
┌────────────────────────────────┐
│  Thêm tài khoản mới            │
├────────────────────────────────┤
│  Ngân hàng:                    │
│  [MBBank              ▼]       │
│                                │
│  Số tài khoản:                 │
│  [3333315012003]               │
│                                │
│  Tên tài khoản:                │
│  [LE MINH VUONG]               │
│                                │
│  Ghi chú:                      │
│  [Tài khoản chính]             │
│                                │
│  ☑ Kích hoạt                   │
│  ☑ Đặt làm mặc định            │
│                                │
│  [  Thêm  ]  [  Hủy  ]         │
└────────────────────────────────┘
```

---

## 🧪 Test

### Test case 1: Thêm tài khoản mới
1. Vào `/admin/bank-accounts`
2. Click "Thêm tài khoản"
3. Điền thông tin
4. Check "Đặt làm mặc định"
5. Click "Thêm"
6. **Kỳ vọng:** Tài khoản mới xuất hiện với badge "Mặc định"

### Test case 2: Thanh toán với tài khoản mới
1. Thêm tài khoản mới và đặt làm mặc định
2. Đặt hàng và chọn thanh toán online
3. Vào trang thanh toán
4. **Kỳ vọng:** QR code hiển thị thông tin tài khoản mới

### Test case 3: Đổi tài khoản mặc định
1. Có 2 tài khoản: A (mặc định), B (không mặc định)
2. Click ⭐ trên tài khoản B
3. **Kỳ vọng:** 
   - B trở thành mặc định
   - A không còn mặc định

### Test case 4: Xóa tài khoản
1. Thử xóa tài khoản mặc định
2. **Kỳ vọng:** Hiển thị lỗi "Không thể xóa tài khoản mặc định"
3. Thử xóa tài khoản không mặc định
4. **Kỳ vọng:** Xóa thành công

---

## 🔐 Bảo mật

- ✅ Chỉ ADMIN mới truy cập được
- ✅ Không thể xóa tài khoản mặc định
- ✅ Tự động active khi set default
- ✅ Transaction-safe (ACID)

---

## 📈 Lợi ích

### Trước:
- ❌ Phải sửa code để đổi tài khoản
- ❌ Phải restart server
- ❌ Chỉ dùng được 1 tài khoản
- ❌ Khó quản lý

### Sau:
- ✅ Đổi tài khoản trực tiếp trên web
- ✅ Không cần restart
- ✅ Quản lý nhiều tài khoản
- ✅ Dễ dàng switch giữa các tài khoản
- ✅ Có thể tạm dừng tài khoản khi cần

---

## 🎉 Hoàn thành!

Giờ bạn có thể:
- ✅ Thêm nhiều tài khoản ngân hàng
- ✅ Chọn tài khoản nào để nhận tiền
- ✅ Đổi tài khoản bất cứ lúc nào
- ✅ Không cần sửa code hay restart server

**Tất cả quản lý trên web!** 🚀
