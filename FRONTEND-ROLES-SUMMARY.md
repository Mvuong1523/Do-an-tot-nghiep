# 👥 TỔNG HỢP GIAO DIỆN THEO VAI TRÒ

## 📊 TỔNG QUAN

Hệ thống có **4 vai trò chính** với giao diện riêng biệt:

```
┌─────────────────────────────────────────────────────────────┐
│                    PHÂN QUYỀN GIAO DIỆN                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. CUSTOMER (Khách hàng)     → /orders, /cart, /products  │
│  2. SALE (Nhân viên bán hàng) → /sales                     │
│  3. ADMIN (Quản trị viên)     → /admin                     │
│  4. SHIPPER (Người giao hàng) → /shipper                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 1️⃣ CUSTOMER (Khách hàng)

### **Giao diện:**
- 🏠 `/` - Trang chủ
- 🛍️ `/products` - Danh sách sản phẩm
- 🛒 `/cart` - Giỏ hàng
- 📦 `/orders` - Đơn hàng của tôi
- 📦 `/orders/[id]` - Chi tiết đơn hàng
- 💳 `/payment/[orderCode]` - Thanh toán

### **Thông tin đơn hàng thấy được:**

#### **Trang `/orders/[id]` - Chi tiết đơn hàng:**

```tsx
✅ Thông tin cơ bản:
   - Mã đơn hàng (orderCode)
   - Trạng thái đơn (status)
   - Trạng thái thanh toán (paymentStatus)
   - Ngày đặt hàng (createdAt)

✅ Sản phẩm:
   - Danh sách sản phẩm
   - Ảnh, tên, giá, số lượng
   - Tổng tiền

✅ Thông tin giao hàng:
   - Người nhận
   - Số điện thoại
   - Email
   - Địa chỉ giao hàng
   - Ghi chú

✅ Thông tin GHN (nếu có):
   - ⏰ Thời gian giao hàng dự kiến (ghnExpectedDeliveryTime)
   - 📦 Mã vận đơn GHN (ghnOrderCode)
   - 🚚 Trạng thái vận chuyển (ghnShippingStatus)
   - 📍 Vị trí hiện tại
   - 📋 Lịch sử di chuyển

✅ Lịch sử đơn hàng:
   - Đơn hàng đã được tạo
   - Đơn hàng đã được xác nhận
   - Đơn hàng đang được giao
   - Đơn hàng đã được giao
```

#### **Component `GHNTracking`:**

```tsx
✅ Hiển thị chi tiết vận chuyển:
   - Trạng thái hiện tại (statusText)
   - Vị trí hiện tại (currentWarehouse)
   - Thời gian giao hàng dự kiến (expectedDeliveryTime)
   - Tiền COD (codAmount)
   - Phí vận chuyển (shippingFee)
   - Lịch sử di chuyển (logs)
   - Ghi chú (note)
```

### **Chức năng:**
- ✅ Xem chi tiết đơn hàng
- ✅ Theo dõi vận chuyển GHN
- ✅ Hủy đơn (nếu chưa giao)
- ✅ Tiếp tục thanh toán (nếu chờ thanh toán)

---

## 2️⃣ SALE (Nhân viên bán hàng)

### **Giao diện:**
- 📊 `/sales` - Dashboard bán hàng
- 📦 `/sales/orders` - Quản lý đơn hàng
- 📤 `/sales/export` - Xuất kho bán hàng

### **Thông tin đơn hàng thấy được:**

#### **Trang `/sales/orders` - Quản lý đơn hàng:**

```tsx
✅ Danh sách đơn hàng:
   - Mã đơn hàng (orderCode)
   - Trạng thái (status)
   - Khách hàng (customerName, customerPhone)
   - Ngày đặt (createdAt)
   - Địa chỉ giao hàng (shippingAddress)
   - Tổng tiền (total)
   - Số lượng sản phẩm

✅ Lọc theo trạng thái:
   - Tất cả
   - Chờ thanh toán
   - Đã xác nhận
   - Đang giao
   - Đã giao
   - Đã hủy

✅ Xem chi tiết đơn:
   - Link đến /orders/[id] (giống khách hàng)
   - Thấy đầy đủ thông tin GHN
```

### **Chức năng:**
- ✅ Xem tất cả đơn hàng
- ✅ Xác nhận đơn hàng (PENDING → CONFIRMED)
- ✅ Đánh dấu đang giao (CONFIRMED → SHIPPING)
- ✅ Xác nhận đã giao (SHIPPING → DELIVERED)
- ✅ Xem chi tiết đơn hàng
- ✅ **Xem thông tin GHN đầy đủ** (mã vận đơn, thời gian dự kiến, trạng thái)

---

## 3️⃣ ADMIN (Quản trị viên)

### **Giao diện:**
- 📊 `/admin` - Dashboard admin
- 📦 `/admin/orders` - Quản lý đơn hàng (chưa có, dùng chung `/sales/orders`)
- 🏢 `/admin/inventory` - Quản lý kho
- 📦 `/admin/products` - Quản lý sản phẩm
- 💰 `/admin/accounting` - Kế toán
- 👥 `/admin/employee-approval` - Phê duyệt nhân viên
- 🏦 `/admin/bank-accounts` - Tài khoản ngân hàng

### **Thông tin đơn hàng thấy được:**

```tsx
✅ Tất cả thông tin như SALE
✅ Thêm quyền:
   - Xem báo cáo tài chính
   - Xem công nợ NCC
   - Quản lý tài khoản ngân hàng
   - Phê duyệt nhân viên
```

### **Chức năng:**
- ✅ Tất cả chức năng của SALE
- ✅ Quản lý toàn bộ hệ thống
- ✅ Xem báo cáo tài chính
- ✅ Quản lý kho hàng
- ✅ Phê duyệt nhân viên

---

## 4️⃣ SHIPPER (Người giao hàng)

### **Giao diện:**
- 🚚 `/shipper` - Dashboard shipper

### **Thông tin đơn hàng thấy được:**

```tsx
✅ Danh sách đơn cần giao:
   - Mã đơn hàng
   - Khách hàng
   - Địa chỉ giao hàng
   - Số điện thoại
   - Tổng tiền
   - Trạng thái

✅ Chức năng:
   - Xem đơn hàng cần giao
   - Cập nhật trạng thái giao hàng
```

---

## 📋 BẢNG SO SÁNH QUYỀN XEM THÔNG TIN GHN

| Thông tin | Customer | SALE | ADMIN | SHIPPER |
|-----------|----------|------|-------|---------|
| **Mã vận đơn GHN** | ✅ | ✅ | ✅ | ✅ |
| **Thời gian giao hàng dự kiến** | ✅ | ✅ | ✅ | ✅ |
| **Trạng thái vận chuyển** | ✅ | ✅ | ✅ | ✅ |
| **Vị trí hiện tại** | ✅ | ✅ | ✅ | ✅ |
| **Lịch sử di chuyển** | ✅ | ✅ | ✅ | ✅ |
| **Tiền COD** | ✅ | ✅ | ✅ | ✅ |
| **Phí vận chuyển** | ✅ | ✅ | ✅ | ✅ |

**→ Tất cả vai trò đều thấy đầy đủ thông tin GHN!** ✅

---

## 🎯 LUỒNG CẬP NHẬT TRẠNG THÁI

### **Khi tài xế GHN đến lấy hàng:**

```
1. Tài xế GHN đến shop
   ↓
2. GHN gửi webhook: status = "picking"
   POST /api/webhooks/ghn
   ↓
3. Backend cập nhật database:
   - order.ghnShippingStatus = "picking"
   - order.status = SHIPPING
   - order.shippedAt = now()
   ↓
4. Frontend tự động hiển thị (tất cả vai trò):
   
   CUSTOMER thấy:
   📦 /orders/[id]
   🚚 Đang lấy hàng
   📍 Kho Hà Nội
   ⏰ Dự kiến giao: 22/12/2024
   
   SALE thấy:
   📦 /sales/orders
   🚚 Đang lấy hàng
   (Có thể xem chi tiết)
   
   ADMIN thấy:
   📦 /admin (dashboard)
   🚚 Đang lấy hàng
   (Có thể xem chi tiết)
```

---

## 📱 GIAO DIỆN CHI TIẾT

### **1. Customer - `/orders/[id]`**

```
┌─────────────────────────────────────────────────────┐
│  📦 Đơn hàng ORD20241220001                         │
│  📅 Đặt ngày: 20/12/2024 10:30                      │
│  [Chờ thanh toán] [💳 Tiếp tục thanh toán]          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  📦 Sản phẩm                                        │
│  • Laptop Dell XPS 13 x1 = 25,000,000đ             │
│  • Mouse Logitech x1 = 500,000đ                     │
│                                                     │
│  Tạm tính: 25,500,000đ                              │
│  Phí ship: 25,000đ                                  │
│  Tổng: 25,525,000đ                                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  📍 Thông tin giao hàng                             │
│  Người nhận: Nguyễn Văn A                           │
│  SĐT: 0123456789                                    │
│  Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM            │
│                                                     │
│  ⏰ Thời gian giao hàng dự kiến                     │
│  📅 22/12/2024 10:00                                │
│                                                     │
│  📦 Mã vận đơn GHN                                  │
│  GHN123456789                                       │
│  Trạng thái: picking                                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  🚚 Theo dõi vận chuyển GHN                         │
│  [🔄 Làm mới]                                       │
│                                                     │
│  📦 Đang lấy hàng                                   │
│  📍 Kho Hà Nội                                      │
│  ⏰ Dự kiến giao: 22/12/2024 10:00                  │
│  Cập nhật lúc: 20/12/2024 10:30                     │
│                                                     │
│  📋 Lịch sử di chuyển:                              │
│  • 20/12/2024 10:30 - Đang lấy hàng (Kho HN)       │
│  • 20/12/2024 09:00 - Chờ lấy hàng                  │
│  • 19/12/2024 15:00 - Đơn hàng đã tạo               │
└─────────────────────────────────────────────────────┘
```

### **2. SALE - `/sales/orders`**

```
┌─────────────────────────────────────────────────────┐
│  📦 Quản lý đơn hàng                    Tổng: 150   │
├─────────────────────────────────────────────────────┤
│  [Tất cả] [Chờ TT] [Đã XN] [Đang giao] [Đã giao]  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  📦 ORD20241220001 [Đang giao]                      │
│  👤 Nguyễn Văn A - 0123456789                       │
│  📅 20/12/2024 10:30                                │
│  📍 123 Đường ABC, Quận 1, TP.HCM                   │
│  💰 25,525,000đ (2 sản phẩm)                        │
│  [Đã giao] [👁️ Chi tiết]                           │
│                                                     │
│  📦 ORD20241220002 [Đã xác nhận]                    │
│  👤 Trần Thị B - 0987654321                         │
│  📅 20/12/2024 11:00                                │
│  📍 456 Đường XYZ, Quận 2, TP.HCM                   │
│  💰 15,000,000đ (1 sản phẩm)                        │
│  [Đang giao] [👁️ Chi tiết]                         │
└─────────────────────────────────────────────────────┘
```

---

## 🔑 KẾT LUẬN

### **Ai thấy được thông tin GHN?**

| Vai trò | Giao diện | Thông tin GHN |
|---------|-----------|---------------|
| **CUSTOMER** | `/orders/[id]` | ✅ Đầy đủ (mã vận đơn, thời gian dự kiến, trạng thái, lịch sử) |
| **SALE** | `/sales/orders` → `/orders/[id]` | ✅ Đầy đủ (giống customer) |
| **ADMIN** | `/admin` → `/orders/[id]` | ✅ Đầy đủ (giống customer) |
| **SHIPPER** | `/shipper` | ✅ Thông tin cơ bản |

### **Thông tin GHN hiển thị:**

1. ✅ **Mã vận đơn GHN** (`ghnOrderCode`)
2. ✅ **Thời gian giao hàng dự kiến** (`ghnExpectedDeliveryTime`) ⭐
3. ✅ **Trạng thái vận chuyển** (`ghnShippingStatus`)
4. ✅ **Vị trí hiện tại** (từ API GHN)
5. ✅ **Lịch sử di chuyển** (từ API GHN)
6. ✅ **Tiền COD** (từ API GHN)
7. ✅ **Phí vận chuyển** (từ API GHN)

### **Cập nhật tự động:**

- ✅ Webhook GHN gửi về khi tài xế đến lấy hàng
- ✅ Backend tự động cập nhật database
- ✅ Frontend hiển thị real-time (refresh để thấy)
- ✅ Tất cả vai trò đều thấy cập nhật

---

**Tất cả vai trò đều thấy đầy đủ thông tin GHN khi xem chi tiết đơn hàng!** 🎯
