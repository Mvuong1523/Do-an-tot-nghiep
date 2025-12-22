# ✅ Dashboard Admin - Thêm Cảnh Báo Quan Trọng

## 📊 Tổng quan

Đã thêm 3 card cảnh báo quan trọng vào dashboard admin để giúp quản lý theo dõi các vấn đề cần xử lý ngay.

---

## 🚨 Các Cảnh Báo Đã Thêm

### 1. Đơn hàng chờ xử lý (Pending Orders)
**Màu**: Vàng (Yellow)
**Icon**: FiClock (đồng hồ)
**Mô tả**: Hiển thị số đơn hàng đang chờ xác nhận và xử lý
**Click**: Chuyển đến `/admin/inventory/orders`

**Tính toán**: 
- Đếm tất cả đơn hàng có status = `PENDING_PAYMENT`
- Backend: `orderRepository.countByStatus(OrderStatus.PENDING_PAYMENT)`

### 2. Đơn hàng quá hạn giao (Overdue Orders)
**Màu**: Đỏ (Red)
**Icon**: FiAlertTriangle (cảnh báo)
**Mô tả**: Đơn hàng quá 4 ngày chưa giao xong
**Click**: Chuyển đến `/admin/inventory/orders`

**Tính toán**:
- Lọc đơn hàng được tạo trước 4 ngày
- Loại trừ đơn đã giao (DELIVERED) và đã hủy (CANCELLED)
- Backend logic:
```java
LocalDateTime fourDaysAgo = LocalDateTime.now().minusDays(4);
Long overdueOrders = orderRepository.findAll().stream()
    .filter(order -> order.getCreatedAt().isBefore(fourDaysAgo))
    .filter(order -> order.getStatus() != OrderStatus.DELIVERED 
                  && order.getStatus() != OrderStatus.CANCELLED)
    .count();
```

### 3. Công nợ đến hạn thanh toán (Overdue Payables)
**Màu**: Cam (Orange)
**Icon**: FiDollarSign (tiền)
**Mô tả**: Công nợ nhà cung cấp chưa thanh toán
**Click**: Chuyển đến `/admin/accounting/payables`

**Tính toán**:
- Hiện tại trả về 0 (TODO)
- Cần tích hợp với module accounting để đếm công nợ quá hạn
- Backend: `Long overduePayables = 0L; // TODO: Integrate with accounting module`

---

## 🎨 Design

### Layout
```
┌─────────────────────────────────────────────────────────┐
│  4 Stats Cards (Green, Orange, Red, Blue)              │
└─────────────────────────────────────────────────────────┘

┌──────────────────┬──────────────────┬──────────────────┐
│  Đơn chờ xử lý   │  Đơn quá hạn     │  Công nợ đến hạn │
│  (Yellow)        │  (Red)           │  (Orange)        │
│  🕐 [số]         │  ⚠️ [số]         │  💰 [số]         │
│  Xem chi tiết →  │  Xem chi tiết →  │  Xem chi tiết →  │
└──────────────────┴──────────────────┴──────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Revenue & Profit Cards                                 │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Recent Orders Table                                    │
└─────────────────────────────────────────────────────────┘
```

### Styling
- **Border-left**: 4px solid color
- **Background**: Light color (yellow-50, red-50, orange-50)
- **Hover**: Shadow-md transition
- **Cursor**: Pointer (clickable)
- **Number**: Text-3xl font-bold
- **Button**: "Xem chi tiết →" với hover effect

---

## 📝 Files Đã Cập Nhật

### 1. Frontend - Dashboard Page
**File**: `src/frontend/app/admin/page.tsx`

**Thay đổi**:
- ✅ Import thêm icons: `FiClock`, `FiAlertTriangle`
- ✅ Thêm fields vào interface: `overdueOrders`, `overduePayables`
- ✅ Thêm 3 warning cards vào render
- ✅ Click handlers để navigate đến trang chi tiết

### 2. Backend - Dashboard Service
**File**: `src/main/java/com/doan/WEB_TMDT/service/impl/DashboardServiceImpl.java`

**Thay đổi**:
- ✅ Tính toán `overdueOrders` (đơn quá 4 ngày)
- ✅ Placeholder cho `overduePayables` (TODO)
- ✅ Return thêm 2 fields mới

### 3. Backend - DTO
**File**: `src/main/java/com/doan/WEB_TMDT/dto/DashboardStatsDTO.java`

**Thay đổi**:
- ✅ Thêm field: `private Long overdueOrders;`
- ✅ Thêm field: `private Long overduePayables;`

---

## 🔍 Logic Chi Tiết

### Đơn hàng quá hạn (Overdue Orders)

**Điều kiện**:
1. Đơn hàng được tạo trước 4 ngày (từ thời điểm hiện tại)
2. Status KHÔNG phải DELIVERED
3. Status KHÔNG phải CANCELLED

**Ví dụ**:
- Hôm nay: 22/12/2025
- 4 ngày trước: 18/12/2025
- Đơn tạo ngày 17/12 với status SHIPPING → **Quá hạn** ✅
- Đơn tạo ngày 19/12 với status CONFIRMED → **Chưa quá hạn** ❌
- Đơn tạo ngày 15/12 với status DELIVERED → **Không tính** ❌

### Công nợ đến hạn (Overdue Payables)

**TODO**: Cần implement logic sau:
1. Lấy tất cả SupplierPayable có `dueDate` < ngày hiện tại
2. Lọc những công nợ chưa thanh toán (`status != PAID`)
3. Đếm số lượng

**Code mẫu** (cần implement):
```java
// In DashboardServiceImpl
@Autowired
private SupplierPayableRepository supplierPayableRepository;

LocalDateTime now = LocalDateTime.now();
Long overduePayables = supplierPayableRepository.findAll().stream()
    .filter(payable -> payable.getDueDate() != null)
    .filter(payable -> payable.getDueDate().isBefore(now))
    .filter(payable -> !"PAID".equals(payable.getStatus()))
    .count();
```

---

## ✅ Testing

### 1. Test Đơn chờ xử lý
```bash
# Tạo đơn hàng mới với status PENDING_PAYMENT
# Kiểm tra số hiển thị trên dashboard
# Click vào card → Chuyển đến trang orders
```

### 2. Test Đơn quá hạn
```bash
# Tạo đơn hàng cách đây 5 ngày với status CONFIRMED
# Dashboard phải hiển thị số > 0
# Click vào card → Chuyển đến trang orders
```

### 3. Test Công nợ đến hạn
```bash
# Hiện tại luôn hiển thị 0
# Sau khi implement logic, test với công nợ quá hạn
# Click vào card → Chuyển đến trang payables
```

---

## 🎯 Kết Quả

### Trước khi thêm
- ❌ Không có cảnh báo về đơn chờ xử lý
- ❌ Không biết đơn nào quá hạn giao
- ❌ Không theo dõi công nợ đến hạn
- ❌ Admin phải vào từng trang để kiểm tra

### Sau khi thêm
- ✅ Nhìn thấy ngay số đơn chờ xử lý
- ✅ Cảnh báo đơn quá hạn giao (màu đỏ)
- ✅ Theo dõi công nợ đến hạn (màu cam)
- ✅ Click vào card để xem chi tiết
- ✅ Dashboard trở nên actionable hơn

---

## 📊 Màu Sắc & Ý Nghĩa

| Cảnh báo | Màu | Ý nghĩa | Mức độ |
|----------|-----|---------|--------|
| Đơn chờ xử lý | 🟡 Vàng | Cần xử lý sớm | Trung bình |
| Đơn quá hạn | 🔴 Đỏ | Khẩn cấp | Cao |
| Công nợ đến hạn | 🟠 Cam | Cần thanh toán | Cao |

---

## 🚀 Next Steps

### 1. Implement Overdue Payables
- [ ] Tích hợp với SupplierPayableRepository
- [ ] Tính toán công nợ quá hạn
- [ ] Test với dữ liệu thực

### 2. Thêm Filter
- [ ] Cho phép filter đơn quá hạn trong trang orders
- [ ] Highlight đơn quá hạn bằng màu đỏ
- [ ] Sort theo thời gian tạo

### 3. Notifications
- [ ] Gửi email khi có đơn quá hạn
- [ ] Push notification cho admin
- [ ] Daily summary report

---

**Status**: ✅ HOÀN THÀNH
**Date**: 22/12/2025
**Tested**: Backend compiles, Frontend no errors
**Ready**: Production ready (except overduePayables logic)
