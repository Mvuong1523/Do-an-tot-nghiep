# 👔 VAI TRÒ NHÂN VIÊN BÁN HÀNG (SALES STAFF)

## 🎯 TỔNG QUAN

**Nhân viên bán hàng (Position: SALE/SALES)** là người **quản lý và xử lý đơn hàng** trong hệ thống TMĐT.

### **Vị trí trong tổ chức:**
```
Admin (Quản trị viên)
    │
    ├─── Nhân viên bán hàng (SALE) ← Bạn đang hỏi
    ├─── Nhân viên kho (WAREHOUSE)
    ├─── CSKH (Customer Service)
    ├─── Kế toán (ACCOUNTANT)
    ├─── Quản lý sản phẩm (PRODUCT_MANAGER)
    └─── Shipper (SHIPPER)
```

---

## 💼 CHỨC NĂNG CHÍNH

### **1. Quản Lý Đơn Hàng** 📦

#### **Xem danh sách đơn hàng:**
- ✅ Xem tất cả đơn hàng trong hệ thống
- ✅ Lọc theo trạng thái (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
- ✅ Tìm kiếm đơn hàng
- ✅ Phân trang (20 đơn/trang)

#### **Xem chi tiết đơn hàng:**
- ✅ Thông tin khách hàng
- ✅ Danh sách sản phẩm
- ✅ Giá tiền, phí ship
- ✅ Địa chỉ giao hàng
- ✅ Trạng thái thanh toán
- ✅ Trạng thái vận chuyển

#### **Cập nhật trạng thái đơn hàng:**
```
PENDING (Chờ xác nhận)
    ↓
CONFIRMED (Đã xác nhận) ← Sales xác nhận
    ↓
SHIPPING (Đang giao) ← Sales đánh dấu
    ↓
DELIVERED (Đã giao) ← Sales xác nhận
```

**Hoặc:**
```
PENDING/CONFIRMED
    ↓
CANCELLED (Đã hủy) ← Sales có thể hủy
```

#### **API Endpoints có quyền:**
```
GET  /api/admin/orders              - Xem tất cả đơn
GET  /api/admin/orders/{id}         - Xem chi tiết
GET  /api/admin/orders/statistics   - Xem thống kê
PUT  /api/admin/orders/{id}/status  - Cập nhật trạng thái
PUT  /api/admin/orders/{id}/shipping - Đánh dấu đang giao
PUT  /api/admin/orders/{id}/delivered - Đánh dấu đã giao
PUT  /api/admin/orders/{id}/cancel  - Hủy đơn
GET  /api/admin/orders/{id}/shipping-status - Xem trạng thái vận chuyển
```

---

### **2. Xem Thống Kê Bán Hàng** 📊

#### **Dashboard hiển thị:**
- 📈 **Tổng đơn hàng** - Số lượng đơn hàng tổng
- ⏳ **Chờ xuất kho** - Đơn đã xác nhận, chờ kho xuất
- 🚚 **Đang giao hàng** - Đơn đang trên đường giao
- 💰 **Doanh thu** - Tổng doanh thu từ đơn hàng

#### **API:**
```
GET /api/orders/stats - Lấy thống kê
```

---

### **3. Xuất Kho Bán Hàng** 📤

**Chức năng:** Xử lý xuất kho cho đơn hàng đã xác nhận

**Quy trình:**
```
1. Khách đặt hàng → PENDING
2. Sales xác nhận → CONFIRMED
3. Sales tạo phiếu xuất kho → Thông báo cho kho
4. Kho xuất hàng → Trừ stockQuantity
5. Sales đánh dấu SHIPPING → Giao cho shipper
6. Shipper giao xong → DELIVERED
```

**Lưu ý:** 
- Sales **không trực tiếp xuất kho**
- Sales **tạo yêu cầu xuất kho** cho nhân viên kho
- Nhân viên kho mới thực sự xuất hàng

---

### **4. Xử Lý Hủy Đơn** ❌

**Có quyền hủy đơn khi:**
- ✅ Khách yêu cầu hủy
- ✅ Không liên lạc được khách
- ✅ Sản phẩm hết hàng
- ✅ Địa chỉ giao hàng không hợp lệ

**Khi hủy đơn, hệ thống tự động:**
- 🔄 Release reserved stock (giải phóng hàng giữ)
- 💰 Tạo refund transaction (nếu đã thanh toán)
- 📧 Gửi email thông báo khách (optional)

**API:**
```
PUT /api/admin/orders/{id}/cancel?reason=Lý do hủy
```

---

### **5. Theo Dõi Vận Chuyển** 🚚

**Chức năng:**
- ✅ Xem trạng thái vận chuyển GHN
- ✅ Xem mã vận đơn
- ✅ Xem lịch sử vận chuyển
- ✅ Cập nhật trạng thái giao hàng

**API:**
```
GET /api/admin/orders/{id}/shipping-status
```

---

## 🔐 QUYỀN TRUY CẬP

### **Có quyền truy cập:**
- ✅ `/api/orders/**` - Xem đơn hàng
- ✅ `/api/admin/orders/**` - Quản lý đơn hàng
- ✅ Dashboard bán hàng (`/sales`)
- ✅ Quản lý đơn hàng (`/sales/orders`)
- ✅ Xuất kho (`/sales/export`)

### **KHÔNG có quyền:**
- ❌ Quản lý sản phẩm (chỉ PRODUCT_MANAGER)
- ❌ Quản lý kho (chỉ WAREHOUSE)
- ❌ Xem báo cáo tài chính (chỉ ACCOUNTANT)
- ❌ Quản lý nhân viên (chỉ ADMIN)
- ❌ Cấu hình hệ thống (chỉ ADMIN)

---

## 📋 QUY TRÌNH LÀM VIỆC ĐIỂN HÌNH

### **Buổi sáng:**
```
1. Đăng nhập hệ thống
2. Xem dashboard → Kiểm tra số đơn mới
3. Xem danh sách đơn PENDING
4. Xác nhận đơn hàng hợp lệ → CONFIRMED
5. Tạo phiếu xuất kho cho các đơn đã xác nhận
```

### **Buổi chiều:**
```
1. Kiểm tra đơn chờ xuất kho
2. Đánh dấu đơn đã xuất kho → SHIPPING
3. Theo dõi trạng thái vận chuyển
4. Xử lý đơn hủy (nếu có)
5. Cập nhật đơn đã giao → DELIVERED
```

### **Cuối ngày:**
```
1. Xem thống kê doanh thu
2. Kiểm tra đơn pending còn lại
3. Báo cáo cho quản lý
```

---

## 🎨 GIAO DIỆN (UI)

### **Dashboard (`/sales`):**
```
┌─────────────────────────────────────────────────────┐
│  Dashboard Bán hàng                                 │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────┐│
│  │ Tổng đơn │  │ Chờ xuất │  │ Đang giao│  │Doanh││
│  │   150    │  │    25    │  │    40    │  │ thu ││
│  └──────────┘  └──────────┘  └──────────┘  └─────┘│
│                                                     │
│  ┌─────────────────────┐  ┌──────────────────────┐ │
│  │ Quản lý đơn hàng    │  │ Xuất kho bán hàng    │ │
│  │ Xem và xử lý đơn    │  │ 25 đơn chờ xử lý     │ │
│  └─────────────────────┘  └──────────────────────┘ │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### **Quản lý đơn hàng (`/sales/orders`):**
```
┌─────────────────────────────────────────────────────┐
│  Quản lý đơn hàng                                   │
├─────────────────────────────────────────────────────┤
│  [Tất cả] [Chờ xác nhận] [Đã xác nhận] [Đang giao] │
│                                                     │
│  ┌─────┬──────────┬──────────┬─────────┬─────────┐ │
│  │ Mã  │ Khách    │ Tổng tiền│ Trạng   │ Hành    │ │
│  │     │          │          │ thái    │ động    │ │
│  ├─────┼──────────┼──────────┼─────────┼─────────┤ │
│  │#001 │Nguyễn A  │1,500,000đ│PENDING  │[Xác nhận]││
│  │#002 │Trần B    │2,300,000đ│CONFIRMED│[Xuất kho]││
│  │#003 │Lê C      │  800,000đ│SHIPPING │[Xem]    │ │
│  └─────┴──────────┴──────────┴─────────┴─────────┘ │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🔄 SO SÁNH VỚI CÁC VỊ TRÍ KHÁC

| Chức năng | Sales | Warehouse | CSKH | Accountant | Shipper |
|-----------|-------|-----------|------|------------|---------|
| Xem đơn hàng | ✅ | ❌ | ✅ | ❌ | ✅ |
| Xác nhận đơn | ✅ | ❌ | ✅ | ❌ | ❌ |
| Hủy đơn | ✅ | ❌ | ✅ | ❌ | ❌ |
| Xuất kho | ❌ | ✅ | ❌ | ❌ | ❌ |
| Nhập kho | ❌ | ✅ | ❌ | ❌ | ❌ |
| Xem tồn kho | ❌ | ✅ | ❌ | ❌ | ❌ |
| Giao hàng | ❌ | ❌ | ❌ | ❌ | ✅ |
| Xem báo cáo TC | ❌ | ❌ | ❌ | ✅ | ❌ |
| Đối soát | ❌ | ❌ | ❌ | ✅ | ❌ |

---

## 💡 TẠI SAO CẦN NHÂN VIÊN BÁN HÀNG?

### **1. Xác nhận đơn hàng:**
- Kiểm tra thông tin khách hàng
- Xác nhận địa chỉ giao hàng
- Liên hệ khách nếu cần
- Đảm bảo đơn hợp lệ trước khi xuất kho

### **2. Điều phối xuất kho:**
- Tạo phiếu xuất kho
- Ưu tiên đơn khẩn cấp
- Phối hợp với kho

### **3. Theo dõi giao hàng:**
- Cập nhật trạng thái
- Xử lý vấn đề phát sinh
- Đảm bảo giao đúng hạn

### **4. Xử lý hủy đơn:**
- Xác nhận lý do hủy
- Xử lý hoàn tiền
- Giải phóng hàng giữ

### **5. Báo cáo doanh số:**
- Theo dõi KPI
- Báo cáo cho quản lý
- Đề xuất cải tiến

---

## 🎯 KPI (Key Performance Indicators)

### **Chỉ số đánh giá:**
1. **Số đơn xử lý/ngày** - Mục tiêu: 50-100 đơn
2. **Tỷ lệ xác nhận đơn** - Mục tiêu: >95%
3. **Thời gian xử lý đơn** - Mục tiêu: <2 giờ
4. **Tỷ lệ hủy đơn** - Mục tiêu: <5%
5. **Doanh thu** - Theo mục tiêu tháng

---

## 📝 USE CASE DIAGRAM

```
┌────────────────────────────────────────────────────┐
│              HỆ THỐNG TMĐT                         │
├────────────────────────────────────────────────────┤
│                                                    │
│    ┌──────────────────┐                           │
│    │ Nhân viên        │                           │
│    │ bán hàng (SALE)  │                           │
│    └────────┬─────────┘                           │
│             │                                      │
│             ├──────→ (Xem danh sách đơn hàng)    │
│             │                                      │
│             ├──────→ (Xem chi tiết đơn hàng)     │
│             │                                      │
│             ├──────→ (Xác nhận đơn hàng)         │
│             │                                      │
│             ├──────→ (Hủy đơn hàng)              │
│             │                                      │
│             ├──────→ (Cập nhật trạng thái đơn)   │
│             │                                      │
│             ├──────→ (Tạo phiếu xuất kho)        │
│             │                                      │
│             ├──────→ (Theo dõi vận chuyển)       │
│             │                                      │
│             └──────→ (Xem thống kê bán hàng)     │
│                                                    │
└────────────────────────────────────────────────────┘
```

---

## 🔗 TƯƠNG TÁC VỚI CÁC BỘ PHẬN KHÁC

```
Khách hàng
    ↓ (Đặt hàng)
Nhân viên bán hàng (SALE)
    ↓ (Xác nhận đơn)
    ├──→ Nhân viên kho (Xuất hàng)
    ├──→ Shipper (Giao hàng)
    ├──→ Kế toán (Đối soát)
    └──→ CSKH (Xử lý khiếu nại)
```

---

## 🎓 KẾT LUẬN

**Nhân viên bán hàng (SALE)** là **cầu nối giữa khách hàng và các bộ phận khác**, đảm bảo đơn hàng được xử lý **chính xác, nhanh chóng và hiệu quả**.

### **Vai trò quan trọng:**
- ✅ Xác nhận và xử lý đơn hàng
- ✅ Điều phối giữa các bộ phận
- ✅ Theo dõi tiến độ giao hàng
- ✅ Xử lý vấn đề phát sinh
- ✅ Đảm bảo trải nghiệm khách hàng tốt

**Nếu không có Sales Staff:**
- ❌ Đơn hàng không được xác nhận kịp thời
- ❌ Kho không biết xuất hàng nào trước
- ❌ Không ai theo dõi trạng thái giao hàng
- ❌ Khách hàng không được hỗ trợ
- ❌ Doanh thu giảm

→ **Sales Staff là vị trí QUAN TRỌNG trong hệ thống TMĐT!** 🎯
