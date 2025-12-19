# 🎯 USE CASE DIAGRAM (DỰA TRÊN CODE THỰC TẾ)

## 📊 TỔNG QUAN HỆ THỐNG

Hệ thống có **4 Actor chính** và **50+ Use Cases** được chia thành 7 module:

```
┌─────────────────────────────────────────────────────────────────┐
│                    HỆ THỐNG QUẢN LÝ TMĐT                        │
│                                                                 │
│  Actors:                                                        │
│  • Khách hàng (Customer)                                        │
│  • Nhân viên (Employee) - 6 positions                           │
│  • Admin                                                        │
│  • Hệ thống bên ngoài (External Systems)                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 👥 ACTORS (Người dùng hệ thống)

### **1. Khách hàng (Customer)**
- Người mua hàng trên website
- Đăng ký, đăng nhập, mua sắm

### **2. Nhân viên (Employee)**
Có 6 vị trí (Position):
- **SALE**: Nhân viên bán hàng
- **CSKH**: Chăm sóc khách hàng
- **PRODUCT_MANAGER**: Quản lý sản phẩm
- **WAREHOUSE**: Nhân viên kho
- **ACCOUNTANT**: Kế toán
- **SHIPPER**: Người giao hàng

### **3. Admin**
- Quản trị viên hệ thống
- Có tất cả quyền

### **4. External Systems**
- GHN API (Giao hàng nhanh)
- SePay (Thanh toán)
- Cloudinary (Lưu ảnh)

---

## 🎭 USE CASE THEO MODULE

## 📦 MODULE 1: AUTH (Xác thực & Phân quyền)

### **Actor: Khách hàng**
```
Khách hàng
    │
    ├──→ (Đăng ký tài khoản)
    │       └── include → (Xác thực OTP)
    │
    ├──→ (Đăng nhập)
    │
    ├──→ (Xem thông tin cá nhân)
    │
    ├──→ (Cập nhật thông tin cá nhân)
    │
    └──→ (Đổi mật khẩu)
```

**API Endpoints:**
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/verify-otp` - Xác thực OTP
- `POST /api/auth/login` - Đăng nhập
- `GET /api/customer/profile` - Xem profile
- `PUT /api/customer/profile` - Cập nhật profile
- `POST /api/auth/change-password` - Đổi mật khẩu

### **Actor: Nhân viên**
```
Nhân viên
    │
    ├──→ (Đăng ký làm việc)
    │       └── extend → (Admin phê duyệt)
    │
    ├──→ (Đăng nhập lần đầu)
    │       └── include → (Đổi mật khẩu bắt buộc)
    │
    └──→ (Truy cập chức năng theo Position)
```

**API Endpoints:**
- `POST /api/employee-registration/register` - Đăng ký NV
- `GET /api/employee-registration/pending` - Xem đơn chờ duyệt (Admin)
- `POST /api/employee-registration/approve/{id}` - Phê duyệt (Admin)
- `POST /api/auth/first-change-password` - Đổi MK lần đầu

---

## 🛍️ MODULE 2: PRODUCT (Sản phẩm)

### **Actor: Khách hàng**
```
Khách hàng
    │
    ├──→ (Xem danh sách sản phẩm)
    │       ├── extend → (Lọc theo danh mục)
    │       ├── extend → (Tìm kiếm sản phẩm)
    │       └── extend → (Sắp xếp sản phẩm)
    │
    ├──→ (Xem chi tiết sản phẩm)
    │       ├── include → (Xem ảnh sản phẩm)
    │       ├── include → (Xem thông số kỹ thuật)
    │       └── include → (Xem tồn kho)
    │
    └──→ (Xem danh mục sản phẩm)
```

**API Endpoints:**
- `GET /api/products` - Danh sách sản phẩm
- `GET /api/products/{id}` - Chi tiết sản phẩm
- `GET /api/products/search?keyword=...` - Tìm kiếm
- `GET /api/categories` - Danh sách danh mục
- `GET /api/categories/{id}` - Chi tiết danh mục

### **Actor: PRODUCT_MANAGER**
```
PRODUCT_MANAGER
    │
    ├──→ (Tạo danh mục)
    │
    ├──→ (Cập nhật danh mục)
    │
    ├──→ (Xóa danh mục)
    │
    ├──→ (Xuất bản sản phẩm từ kho)
    │       └── include → (Upload ảnh sản phẩm)
    │
    ├──→ (Cập nhật sản phẩm)
    │
    ├──→ (Ẩn/Hiện sản phẩm)
    │
    └──→ (Xóa sản phẩm)
```

**API Endpoints:**
- `POST /api/categories` - Tạo danh mục
- `PUT /api/categories/{id}` - Cập nhật danh mục
- `DELETE /api/categories/{id}` - Xóa danh mục
- `POST /api/products/publish` - Xuất bản sản phẩm
- `PUT /api/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/products/{id}` - Xóa sản phẩm

---

## 🛒 MODULE 3: CART (Giỏ hàng)

### **Actor: Khách hàng**
```
Khách hàng
    │
    ├──→ (Thêm sản phẩm vào giỏ)
    │
    ├──→ (Xem giỏ hàng)
    │
    ├──→ (Cập nhật số lượng)
    │
    ├──→ (Xóa sản phẩm khỏi giỏ)
    │
    └──→ (Xóa toàn bộ giỏ hàng)
```

**API Endpoints:**
- `POST /api/cart/add` - Thêm vào giỏ
- `GET /api/cart` - Xem giỏ hàng
- `PUT /api/cart/update/{itemId}` - Cập nhật số lượng
- `DELETE /api/cart/remove/{itemId}` - Xóa sản phẩm
- `DELETE /api/cart/clear` - Xóa toàn bộ

---


## 📦 MODULE 4: ORDER (Đơn hàng)

### **Actor: Khách hàng**
```
Khách hàng
    │
    ├──→ (Tạo đơn hàng)
    │       ├── include → (Tính phí vận chuyển)
    │       └── include → (Giữ hàng tạm thời)
    │
    ├──→ (Xem danh sách đơn hàng)
    │
    ├──→ (Xem chi tiết đơn hàng)
    │       └── include → (Theo dõi vận chuyển)
    │
    └──→ (Hủy đơn hàng)
```

**API Endpoints:**
- `POST /api/orders` - Tạo đơn hàng
- `GET /api/orders` - Danh sách đơn hàng
- `GET /api/orders/{id}` - Chi tiết đơn hàng
- `PUT /api/orders/{id}/cancel` - Hủy đơn

### **Actor: SALE (Nhân viên bán hàng)**
```
SALE
    │
    ├──→ (Xem tất cả đơn hàng)
    │       ├── extend → (Lọc theo trạng thái)
    │       └── extend → (Tìm kiếm đơn hàng)
    │
    ├──→ (Xem chi tiết đơn hàng)
    │
    ├──→ (Xác nhận đơn hàng)
    │
    ├──→ (Cập nhật trạng thái đơn)
    │       ├── PENDING → CONFIRMED
    │       ├── CONFIRMED → SHIPPING
    │       └── SHIPPING → DELIVERED
    │
    ├──→ (Hủy đơn hàng)
    │       └── include → (Giải phóng hàng giữ)
    │
    ├──→ (Xem thống kê đơn hàng)
    │
    └──→ (Theo dõi trạng thái vận chuyển GHN)
```

**API Endpoints:**
- `GET /api/admin/orders` - Xem tất cả đơn
- `GET /api/admin/orders/{id}` - Chi tiết đơn
- `PUT /api/admin/orders/{id}/confirm` - Xác nhận đơn
- `PUT /api/admin/orders/{id}/status` - Cập nhật trạng thái
- `PUT /api/admin/orders/{id}/cancel` - Hủy đơn
- `GET /api/admin/orders/statistics` - Thống kê
- `GET /api/admin/orders/{id}/shipping-status` - Trạng thái GHN

### **Actor: SHIPPER**
```
SHIPPER
    │
    ├──→ (Xem đơn hàng cần giao)
    │
    ├──→ (Nhận đơn hàng)
    │
    ├──→ (Cập nhật trạng thái giao hàng)
    │
    └──→ (Xác nhận đã giao)
```

---

## 💳 MODULE 5: PAYMENT (Thanh toán)

### **Actor: Khách hàng**
```
Khách hàng
    │
    ├──→ (Tạo thanh toán)
    │       ├── extend → (Thanh toán COD)
    │       └── extend → (Thanh toán SePay)
    │               └── include → (Tạo QR Code)
    │
    ├──→ (Xem thông tin thanh toán)
    │
    └──→ (Kiểm tra trạng thái thanh toán)
```

**API Endpoints:**
- `POST /api/payment/create` - Tạo thanh toán
- `GET /api/payment/{orderCode}` - Xem thanh toán
- `GET /api/payment/{orderCode}/status` - Kiểm tra trạng thái

### **Actor: SePay (External System)**
```
SePay
    │
    └──→ (Gửi webhook thanh toán)
            └── trigger → (Cập nhật trạng thái đơn)
```

**API Endpoints:**
- `POST /api/webhooks/sepay` - Nhận webhook từ SePay

### **Actor: Admin**
```
Admin
    │
    ├──→ (Quản lý tài khoản ngân hàng)
    │       ├── (Thêm tài khoản)
    │       ├── (Cập nhật tài khoản)
    │       ├── (Xóa tài khoản)
    │       └── (Đặt tài khoản mặc định)
    │
    └──→ (Xem danh sách thanh toán)
```

**API Endpoints:**
- `POST /api/admin/bank-accounts` - Thêm TK ngân hàng
- `PUT /api/admin/bank-accounts/{id}` - Cập nhật TK
- `DELETE /api/admin/bank-accounts/{id}` - Xóa TK
- `PUT /api/admin/bank-accounts/{id}/set-default` - Đặt mặc định

---

## 📦 MODULE 6: INVENTORY (Quản lý kho)

### **Actor: WAREHOUSE (Nhân viên kho)**
```
WAREHOUSE
    │
    ├──→ (Quản lý nhà cung cấp)
    │       ├── (Thêm NCC)
    │       ├── (Cập nhật NCC)
    │       └── (Xem danh sách NCC)
    │
    ├──→ (Tạo phiếu nhập kho - PO)
    │       ├── include → (Chọn NCC)
    │       ├── include → (Thêm sản phẩm)
    │       └── include → (Nhập serial/IMEI)
    │
    ├──→ (Nhập hàng vào kho)
    │       ├── include → (Quét serial)
    │       ├── include → (Cập nhật tồn kho)
    │       └── include → (Tạo công nợ NCC)
    │
    ├──→ (Tạo phiếu xuất kho)
    │       ├── extend → (Xuất bán hàng)
    │       ├── extend → (Xuất hủy hàng)
    │       └── extend → (Xuất bảo hành)
    │
    ├──→ (Xuất hàng khỏi kho)
    │       ├── include → (Chọn serial xuất)
    │       └── include → (Cập nhật tồn kho)
    │
    ├──→ (Xem tồn kho)
    │       ├── (Tồn thực tế)
    │       ├── (Hàng đang giữ)
    │       └── (Hàng có thể bán)
    │
    ├──→ (Kiểm kê kho)
    │
    ├──→ (Quản lý sản phẩm kho)
    │       ├── (Thêm sản phẩm mới)
    │       ├── (Cập nhật thông tin)
    │       ├── (Upload ảnh)
    │       └── (Thêm thông số kỹ thuật)
    │
    └──→ (Import Excel sản phẩm)
```

**API Endpoints:**
- `POST /api/inventory/suppliers` - Thêm NCC
- `GET /api/inventory/suppliers` - Danh sách NCC
- `POST /api/inventory/orders/purchase` - Tạo PO
- `POST /api/inventory/orders/purchase/{id}/receive` - Nhập hàng
- `POST /api/inventory/orders/export` - Tạo phiếu xuất
- `POST /api/inventory/orders/export/{id}/complete` - Xuất hàng
- `GET /api/inventory/stock` - Xem tồn kho
- `POST /api/inventory/products` - Thêm sản phẩm kho
- `POST /api/inventory/products/import-excel` - Import Excel

---


## 💰 MODULE 7: ACCOUNTING (Kế toán)

### **Actor: ACCOUNTANT (Kế toán)**
```
ACCOUNTANT
    │
    ├──→ (Xem giao dịch tài chính)
    │       ├── extend → (Lọc theo loại)
    │       ├── extend → (Lọc theo thời gian)
    │       └── extend → (Xuất Excel)
    │
    ├──→ (Xem báo cáo tài chính)
    │       ├── (Báo cáo doanh thu)
    │       ├── (Báo cáo lợi nhuận)
    │       ├── (Báo cáo chi phí)
    │       └── (Báo cáo tổng hợp)
    │
    ├──→ (Quản lý công nợ NCC)
    │       ├── (Xem danh sách công nợ)
    │       ├── (Xem chi tiết công nợ)
    │       ├── (Lọc theo trạng thái)
    │       └── (Lọc theo NCC)
    │
    ├──→ (Thanh toán cho NCC)
    │       ├── include → (Chọn công nợ)
    │       ├── include → (Nhập số tiền)
    │       ├── include → (Chọn phương thức)
    │       └── include → (Cập nhật công nợ)
    │
    ├──→ (Đối soát thanh toán)
    │       ├── (So sánh hệ thống vs Gateway)
    │       ├── (Xác định chênh lệch)
    │       └── (Xử lý chênh lệch)
    │
    ├──→ (Quản lý kỳ kế toán)
    │       ├── (Tạo kỳ kế toán)
    │       ├── (Đóng kỳ kế toán)
    │       └── (Xem báo cáo kỳ)
    │
    └──→ (Quản lý báo cáo thuế)
            ├── (Tạo báo cáo thuế)
            ├── (Tính thuế VAT)
            ├── (Tính thuế TNDN)
            └── (Nộp báo cáo)
```

**API Endpoints:**
- `GET /api/accounting/transactions` - Xem giao dịch
- `GET /api/accounting/transactions/export` - Xuất Excel
- `GET /api/accounting/financial-statement` - Báo cáo TC
- `GET /api/accounting/financial-statement/profit-loss` - Lãi/Lỗ
- `GET /api/accounting/payables` - Danh sách công nợ
- `GET /api/accounting/payables/{id}` - Chi tiết công nợ
- `POST /api/accounting/payables/{id}/pay` - Thanh toán NCC
- `GET /api/accounting/reconciliation` - Đối soát
- `POST /api/accounting/periods` - Tạo kỳ KT
- `PUT /api/accounting/periods/{id}/close` - Đóng kỳ
- `POST /api/accounting/tax-reports` - Tạo báo cáo thuế

---

## 🚚 MODULE 8: SHIPPING (Vận chuyển)

### **Actor: Hệ thống**
```
Hệ thống
    │
    ├──→ (Tính phí vận chuyển)
    │       └── integrate → GHN API
    │
    ├──→ (Tạo đơn vận chuyển GHN)
    │       └── integrate → GHN API
    │
    └──→ (Theo dõi trạng thái vận chuyển)
            └── integrate → GHN API
```

**API Endpoints:**
- `POST /api/shipping/calculate-fee` - Tính phí ship
- `POST /api/shipping/create-order` - Tạo đơn GHN
- `GET /api/shipping/track/{orderCode}` - Theo dõi

### **Actor: GHN (External System)**
```
GHN
    │
    └──→ (Gửi webhook cập nhật trạng thái)
            └── trigger → (Cập nhật trạng thái đơn hàng)
```

**API Endpoints:**
- `POST /api/webhooks/ghn` - Nhận webhook từ GHN

---

## 📁 MODULE 9: FILE (Quản lý file)

### **Actor: Nhân viên**
```
Nhân viên
    │
    ├──→ (Upload ảnh sản phẩm)
    │       └── integrate → Cloudinary
    │
    ├──→ (Upload nhiều ảnh)
    │       └── integrate → Cloudinary
    │
    └──→ (Xóa ảnh)
            └── integrate → Cloudinary
```

**API Endpoints:**
- `POST /api/files/upload` - Upload 1 ảnh
- `POST /api/files/upload-multiple` - Upload nhiều ảnh
- `DELETE /api/files/delete` - Xóa ảnh

---

## 🎯 SƠ ĐỒ USE CASE TỔNG HỢP

```
┌────────────────────────────────────────────────────────────────────┐
│                    HỆ THỐNG QUẢN LÝ TMĐT                           │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ Khách hàng   │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Đăng ký/Đăng nhập)                                 │
│         ├──→ (Xem sản phẩm)                                      │
│         ├──→ (Quản lý giỏ hàng)                                  │
│         ├──→ (Đặt hàng)                                          │
│         ├──→ (Thanh toán)                                        │
│         ├──→ (Theo dõi đơn hàng)                                 │
│         └──→ (Quản lý tài khoản)                                 │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ SALE         │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Quản lý đơn hàng)                                  │
│         ├──→ (Xác nhận đơn)                                      │
│         ├──→ (Cập nhật trạng thái)                               │
│         └──→ (Xem thống kê)                                      │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ WAREHOUSE    │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Quản lý NCC)                                       │
│         ├──→ (Nhập kho)                                          │
│         ├──→ (Xuất kho)                                          │
│         ├──→ (Quản lý tồn kho)                                   │
│         └──→ (Kiểm kê)                                           │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ PRODUCT_MGR  │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Quản lý danh mục)                                  │
│         ├──→ (Xuất bản sản phẩm)                                 │
│         └──→ (Quản lý sản phẩm)                                  │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ ACCOUNTANT   │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Xem giao dịch)                                     │
│         ├──→ (Báo cáo tài chính)                                 │
│         ├──→ (Quản lý công nợ)                                   │
│         ├──→ (Thanh toán NCC)                                    │
│         └──→ (Báo cáo thuế)                                      │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ SHIPPER      │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Xem đơn cần giao)                                  │
│         ├──→ (Nhận đơn)                                          │
│         └──→ (Cập nhật giao hàng)                                │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ Admin        │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Phê duyệt nhân viên)                               │
│         ├──→ (Quản lý tài khoản NH)                              │
│         └──→ (Tất cả chức năng)                                  │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ GHN API      │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         ├──→ (Tính phí ship)                                     │
│         ├──→ (Tạo đơn vận chuyển)                                │
│         └──→ (Cập nhật trạng thái)                               │
│                                                                    │
│  ┌──────────────┐                                                 │
│  │ SePay        │                                                 │
│  └──────┬───────┘                                                 │
│         │                                                          │
│         └──→ (Xác nhận thanh toán)                               │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

---

## 📊 THỐNG KÊ USE CASE

| Actor | Số Use Case | Module chính |
|-------|-------------|--------------|
| **Khách hàng** | 15+ | Auth, Product, Cart, Order, Payment |
| **SALE** | 10+ | Order, Shipping |
| **WAREHOUSE** | 20+ | Inventory, Product |
| **PRODUCT_MANAGER** | 10+ | Product, Category |
| **ACCOUNTANT** | 15+ | Accounting, Financial |
| **SHIPPER** | 5+ | Order, Shipping |
| **Admin** | ALL | All modules |
| **GHN API** | 3 | Shipping |
| **SePay** | 1 | Payment |
| **TỔNG** | **50+** | 9 modules |

---

## 🔐 PHÂN QUYỀN THEO POSITION

| Chức năng | Customer | SALE | WAREHOUSE | PRODUCT_MGR | ACCOUNTANT | SHIPPER | Admin |
|-----------|----------|------|-----------|-------------|------------|---------|-------|
| Mua hàng | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Quản lý đơn | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ |
| Quản lý kho | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Quản lý SP | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ✅ |
| Kế toán | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |
| Giao hàng | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |

---

**File này được tạo dựa trên code thực tế từ các Controller trong dự án!** ✅

**Tổng số:**
- **9 Actors** (4 người + 5 hệ thống)
- **50+ Use Cases**
- **100+ API Endpoints**
- **7 Modules chính**
