# Cập nhật Module Kế toán - Theo yêu cầu anh Minh Vương

## 🔧 Các vấn đề đã sửa

### 1. ✅ Đơn hàng CONFIRMED đã thanh toán được ghi nhận doanh thu

**Vấn đề:** Đơn hàng đã xác nhận (CONFIRMED) và đã thanh toán nhưng chưa xuất kho vẫn phải hiển thị trong báo cáo tài chính.

**Giải pháp:**
- Cập nhật `OrderEventListener` để ghi nhận doanh thu ngay khi:
  - Đơn hàng chuyển sang CONFIRMED + PAID
  - Hoặc đơn hàng DELIVERED
- Logic: `if (newStatus == OrderStatus.CONFIRMED && order.getPaymentStatus() == PaymentStatus.PAID)`

**File:** `src/main/java/com/doan/WEB_TMDT/module/accounting/listener/OrderEventListener.java`

---

### 2. ✅ Tiền về tài khoản được kê khai đầy đủ

**Vấn đề:** Tiền từ khách hàng thanh toán phải được ghi nhận vào hệ thống kế toán.

**Giải pháp:**
- Tạo `FinancialTransaction` tự động khi:
  - Đơn hàng CONFIRMED + PAID
  - Đơn hàng DELIVERED
- Ghi nhận:
  - Doanh thu bán hàng (SALES)
  - Doanh thu vận chuyển (SHIPPING)
  - Phí thanh toán (PAYMENT_FEE)
  - Giá vốn hàng bán (COST_OF_GOODS)

---

### 3. ✅ Module kế toán như báo cáo tài chính chuẩn

**Vấn đề:** Cần làm module kế toán giống báo cáo tài chính thực tế.

**Giải pháp:** Tạo báo cáo tài chính chuẩn gồm 5 phần:

#### I. DOANH THU (Revenue)
- Tổng doanh thu
- Doanh thu bán hàng
- Doanh thu vận chuyển
- Số đơn hàng

#### II. CHI PHÍ (Expenses)
- Tổng chi phí
- Giá vốn hàng bán (COGS)
- Chi phí vận chuyển
- Phí thanh toán
- Chi phí hoạt động
- Chi phí khác

#### III. LỢI NHUẬN (Profit)
- Lợi nhuận gộp = Doanh thu - Giá vốn
- Lợi nhuận hoạt động = Lợi nhuận gộp - Chi phí HĐ
- Lợi nhuận ròng = Lợi nhuận HĐ - Thuế
- Tỷ suất lợi nhuận (%)

#### IV. CÔNG NỢ (Payables)
- Tổng công nợ phải trả
- Đã thanh toán
- Còn nợ
- Số công nợ quá hạn
- Số tiền quá hạn

#### V. DÒNG TIỀN (Cash Flow)
- Tiền vào (từ khách hàng)
- Tiền ra (trả NCC, chi phí)
- Dòng tiền ròng
- Số dư đầu kỳ
- Số dư cuối kỳ

**Files mới:**
- `FinancialStatementResponse.java` - DTO báo cáo tài chính
- `FinancialStatementService.java` - Interface service
- `FinancialStatementServiceImpl.java` - Implementation
- `FinancialStatementController.java` - API endpoints

---

### 4. ✅ Kiểm soát ở API level

**Vấn đề:** Cần validation và kiểm soát chặt chẽ ở tầng API.

**Giải pháp:**

#### A. Validation ngày tháng
```java
// Ngày bắt đầu phải trước ngày kết thúc
if (startDate.isAfter(endDate)) {
    return ApiResponse.error("Ngày bắt đầu phải trước ngày kết thúc");
}

// Không cho phép query quá 1 năm
if (startDate.plusYears(1).isBefore(endDate)) {
    return ApiResponse.error("Khoảng thời gian không được vượt quá 1 năm");
}
```

#### B. Validation tháng/quý/năm
```java
// Tháng: 1-12
if (month < 1 || month > 12) {
    return ApiResponse.error("Tháng không hợp lệ (1-12)");
}

// Quý: 1-4
if (quarter < 1 || quarter > 4) {
    return ApiResponse.error("Quý không hợp lệ (1-4)");
}

// Năm: 2000 - năm hiện tại + 1
if (year < 2000 || year > LocalDate.now().getYear() + 1) {
    return ApiResponse.error("Năm không hợp lệ");
}
```

#### C. Authorization
```java
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
```
- Chỉ Admin và Accountant mới xem được báo cáo tài chính

---

## 📊 API Endpoints mới

### 1. Báo cáo tổng hợp
```
GET /api/accounting/financial-statement?startDate=2024-01-01&endDate=2024-12-31
```

### 2. Báo cáo doanh thu
```
GET /api/accounting/financial-statement/revenue?startDate=2024-01-01&endDate=2024-12-31
```

### 3. Báo cáo chi phí
```
GET /api/accounting/financial-statement/expenses?startDate=2024-01-01&endDate=2024-12-31
```

### 4. Báo cáo lợi nhuận
```
GET /api/accounting/financial-statement/profit?startDate=2024-01-01&endDate=2024-12-31
```

### 5. Báo cáo dòng tiền
```
GET /api/accounting/financial-statement/cash-flow?startDate=2024-01-01&endDate=2024-12-31
```

### 6. Dashboard (tháng hiện tại)
```
GET /api/accounting/financial-statement/dashboard
```

### 7. Báo cáo theo tháng
```
GET /api/accounting/financial-statement/monthly/2024/12
```

### 8. Báo cáo theo quý
```
GET /api/accounting/financial-statement/quarterly/2024/4
```

### 9. Báo cáo theo năm
```
GET /api/accounting/financial-statement/yearly/2024
```

---

## 🔄 Luồng hoạt động

### Khi khách hàng đặt hàng và thanh toán:

1. **Tạo đơn hàng** → Status: PENDING_PAYMENT
2. **Thanh toán thành công** → Status: CONFIRMED, PaymentStatus: PAID
3. **Tự động ghi nhận doanh thu:**
   - ✅ Tạo FinancialTransaction (REVENUE - SALES)
   - ✅ Tạo FinancialTransaction (REVENUE - SHIPPING)
   - ✅ Tạo FinancialTransaction (EXPENSE - PAYMENT_FEE)
   - ✅ Tạo FinancialTransaction (EXPENSE - COST_OF_GOODS)
4. **Xuất kho** → Status: PROCESSING → SHIPPING
5. **Giao hàng** → Status: DELIVERED
6. **Hoàn thành** → Status: COMPLETED

### Khi nhập hàng từ NCC:

1. **Tạo PO** → Status: CREATED
2. **Hoàn tất nhập hàng** → Status: RECEIVED
3. **Tự động tạo công nợ:**
   - ✅ Tạo SupplierPayable
   - ✅ Tính ngày hạn = ngày nhập + số ngày nợ
   - ✅ Status: UNPAID
4. **Thanh toán NCC:**
   - ✅ Tạo SupplierPayment
   - ✅ Cập nhật SupplierPayable
   - ✅ Status: PARTIAL hoặc PAID

---

## 📈 Công thức tính toán

### Lợi nhuận gộp (Gross Profit)
```
Lợi nhuận gộp = Doanh thu - Giá vốn hàng bán
```

### Lợi nhuận hoạt động (Operating Profit)
```
Lợi nhuận HĐ = Lợi nhuận gộp - Chi phí hoạt động - Chi phí khác
```

### Lợi nhuận ròng (Net Profit)
```
Lợi nhuận ròng = Lợi nhuận HĐ - Phí thanh toán - Chi phí vận chuyển
```

### Tỷ suất lợi nhuận (Profit Margin)
```
Tỷ suất LN = (Lợi nhuận ròng / Doanh thu) × 100%
```

### Dòng tiền ròng (Net Cash Flow)
```
Dòng tiền ròng = Tiền vào - Tiền ra
```

---

## ✅ Checklist hoàn thành

- [x] Ghi nhận doanh thu khi CONFIRMED + PAID
- [x] Ghi nhận doanh thu khi DELIVERED
- [x] Tạo báo cáo tài chính chuẩn (5 phần)
- [x] Validation ngày tháng ở API
- [x] Validation tháng/quý/năm
- [x] Authorization (chỉ Admin/Accountant)
- [x] API endpoints đầy đủ
- [x] Tính toán lợi nhuận chính xác
- [x] Tính toán dòng tiền
- [x] Tích hợp công nợ NCC vào báo cáo

---

## 🎯 Kết quả

Sau khi cập nhật:

1. ✅ **Đơn hàng CONFIRMED + PAID** → Hiển thị trong báo cáo doanh thu
2. ✅ **Tiền về tài khoản** → Được ghi nhận đầy đủ
3. ✅ **Báo cáo tài chính** → Chuẩn như báo cáo thực tế
4. ✅ **API có validation** → Kiểm soát chặt chẽ
5. ✅ **Chỉ Admin/Accountant** → Xem được báo cáo

---

## 📝 Lưu ý

- Module hoạt động tự động, không cần can thiệp thủ công
- Dữ liệu được tính toán real-time từ database
- Có thể xuất Excel/PDF (cần implement thêm)
- Dashboard tự động lấy dữ liệu tháng hiện tại
- Báo cáo có thể filter theo tháng/quý/năm

---

**Đã sẵn sàng để test! 🚀**
