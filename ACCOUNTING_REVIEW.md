# Kiểm tra toàn bộ nghiệp vụ kế toán

## ✅ Phần đã hoàn thiện

### 1. Frontend Accounting Module
**Các trang đã có:**
- ✅ `/admin/accounting` - Dashboard kế toán
- ✅ `/admin/accounting/reconciliation` - Đối soát thanh toán
- ✅ `/admin/accounting/reports` - Báo cáo tài chính
- ✅ `/admin/accounting/periods` - Quản lý kỳ báo cáo

**Tính năng:**
- ✅ Thống kê tổng quan (doanh thu, đối soát, sai lệch)
- ✅ Đối soát thanh toán với cổng thanh toán
- ✅ Import file CSV từ cổng thanh toán
- ✅ Báo cáo tài chính chi tiết (theo đơn/ngày/tháng)
- ✅ Xuất Excel báo cáo
- ✅ Chốt sổ kỳ kế toán
- ✅ Mở khóa kỳ (chỉ Admin)

### 2. Backend Accounting Module
**Entities:**
- ✅ `PaymentReconciliation` - Đối soát thanh toán
- ✅ `AccountingPeriod` - Kỳ báo cáo
- ✅ `ReconciliationStatus` - Trạng thái đối soát
- ✅ `PeriodStatus` - Trạng thái kỳ

**API Endpoints:**
- ✅ `GET /api/accounting/stats` - Thống kê
- ✅ `POST /api/accounting/payment-reconciliation` - Đối soát thanh toán
- ✅ `POST /api/accounting/payment-reconciliation/import` - Import CSV
- ✅ `GET /api/accounting/shipping-reconciliation` - Đối soát vận chuyển (TODO)
- ✅ `GET /api/accounting/reports` - Báo cáo tài chính
- ✅ `GET /api/accounting/reports/export` - Xuất Excel
- ✅ `GET /api/accounting/periods` - Danh sách kỳ
- ✅ `POST /api/accounting/periods/{id}/close` - Chốt kỳ
- ✅ `POST /api/accounting/periods/{id}/reopen` - Mở khóa kỳ (Admin only)

**Nghiệp vụ tính toán:**
- ✅ Doanh thu = Tổng tiền đơn hàng
- ✅ VAT = 10% doanh thu
- ✅ Giá vốn = 60% subtotal (giả định)
- ✅ Phí vận chuyển = shippingFee
- ✅ Phí cổng thanh toán = 2% doanh thu
- ✅ Lợi nhuận gộp = Doanh thu - VAT - Giá vốn - Phí VC - Phí TT
- ✅ Thuế TNDN = 20% lợi nhuận gộp
- ✅ Lợi nhuận ròng = Lợi nhuận gộp - Thuế TNDN

### 3. Phân quyền
**Đã cấu hình:**
- ✅ ADMIN: Full quyền accounting
- ✅ ACCOUNTANT (EMPLOYEE): Truy cập accounting, không mở khóa kỳ
- ✅ Security: `@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")`

### 4. Navigation & Layout
**Đã fix:**
- ✅ HorizontalNav có menu ACCOUNTANT riêng
- ✅ Admin layout xử lý đúng role ACCOUNTANT
- ✅ RootLayoutClient không hiển thị Header customer cho accounting
- ✅ Header không hiển thị menu admin cho accountant

## ⚠️ Vấn đề cần lưu ý

### 1. Giá vốn hàng bán (COGS)
**Vấn đề:** Hiện tại giả định giá vốn = 60% subtotal
**Cần:** 
- Lưu giá nhập thực tế trong `ProductDetail.purchasePrice`
- Tính COGS = Tổng (số lượng × giá nhập) của các sản phẩm trong đơn
- Cập nhật khi xuất kho

**Giải pháp:**
```java
// Trong OrderItem, cần thêm field:
private Double purchasePrice; // Giá nhập tại thời điểm bán

// Khi tạo đơn hàng, lưu giá nhập:
orderItem.setPurchasePrice(productDetail.getPurchasePrice());

// Khi tính báo cáo:
double costOfGoods = order.getItems().stream()
    .mapToDouble(item -> item.getQuantity() * item.getPurchasePrice())
    .sum();
```

### 2. Đối soát vận chuyển
**Vấn đề:** Chưa implement
**Cần:**
- Tích hợp API đơn vị vận chuyển (GHN, GHTK, etc.)
- So sánh phí vận chuyển hệ thống vs thực tế
- Theo dõi COD (thu hộ)

### 3. Tự động tạo kỳ báo cáo
**Vấn đề:** Chưa có tự động tạo kỳ hàng tháng
**Cần:**
- Scheduled job tạo kỳ mới mỗi đầu tháng
- Tính toán tự động doanh thu, sai lệch

**Giải pháp:**
```java
@Scheduled(cron = "0 0 0 1 * ?") // Chạy 00:00 ngày 1 hàng tháng
public void createMonthlyPeriod() {
    LocalDate now = LocalDate.now();
    LocalDate startDate = now.withDayOfMonth(1);
    LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
    
    AccountingPeriod period = AccountingPeriod.builder()
        .name("Kỳ " + now.getMonthValue() + "/" + now.getYear())
        .startDate(startDate)
        .endDate(endDate)
        .status(PeriodStatus.OPEN)
        .createdAt(LocalDateTime.now())
        .build();
    
    periodRepo.save(period);
}
```

### 4. Kiểm tra sai lệch trước khi chốt kỳ
**Đã có:** Kiểm tra sai số > 15%
**Cần thêm:**
- Cảnh báo nếu sai lệch > 5 triệu đồng
- Yêu cầu ghi chú lý do nếu chốt kỳ có sai lệch

### 5. Audit log
**Vấn đề:** Chưa có log chi tiết
**Cần:**
- Log mọi thao tác chốt/mở khóa kỳ
- Log import đối soát
- Log xuất báo cáo

## 🔧 Vấn đề Sales Export cần fix

### 1. API chưa có
**Cần tạo các endpoint:**

```java
// OrderController hoặc SalesController
@GetMapping("/api/orders")
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'SALES')")
public ApiResponse getOrders(
    @RequestParam(required = false) String status,
    Authentication authentication) {
    // Nếu là SALES, lấy tất cả đơn
    // Nếu là CUSTOMER, chỉ lấy đơn của mình
}

@PostMapping("/api/orders/{orderId}/export")
@PreAuthorize("hasAnyAuthority('ADMIN', 'SALES')")
public ApiResponse exportOrder(@PathVariable Long orderId) {
    // Cập nhật trạng thái đơn hàng
    // Tạo phiếu xuất kho
    // Trừ tồn kho
}

@GetMapping("/api/orders/stats")
@PreAuthorize("hasAnyAuthority('ADMIN', 'SALES')")
public ApiResponse getOrderStats() {
    // Thống kê cho dashboard sales
}
```

### 2. Cập nhật SecurityConfig
**Cần thêm:**
```java
.requestMatchers("/api/orders/**").hasAnyAuthority("CUSTOMER", "ADMIN", "SALES")
```

### 3. Nghiệp vụ xuất kho bán hàng
**Flow:**
1. Sales xem đơn hàng CONFIRMED
2. Nhấn "Xuất kho"
3. Hệ thống:
   - Kiểm tra tồn kho đủ không
   - Tạo phiếu xuất kho
   - Trừ tồn kho
   - Cập nhật trạng thái đơn → SHIPPING
   - Ghi nhận giá vốn (purchasePrice) vào OrderItem

## 📊 Tổng kết

### Hoàn thiện (90%)
- ✅ Frontend accounting đầy đủ
- ✅ Backend accounting cơ bản
- ✅ Phân quyền đúng
- ✅ Navigation & layout

### Cần bổ sung (10%)
- ⚠️ Giá vốn thực tế (quan trọng)
- ⚠️ API sales export (quan trọng)
- ⚠️ Đối soát vận chuyển
- ⚠️ Tự động tạo kỳ
- ⚠️ Audit log

### Ưu tiên cao
1. **Lưu giá vốn thực tế** - Ảnh hưởng trực tiếp đến báo cáo lợi nhuận
2. **API sales export** - Cần cho nghiệp vụ bán hàng
3. **Cập nhật SecurityConfig** - Cho phép SALES truy cập orders

### Ưu tiên trung bình
4. Tự động tạo kỳ báo cáo
5. Đối soát vận chuyển
6. Audit log

## 🎯 Khuyến nghị

### Ngay lập tức
1. Thêm `purchasePrice` vào OrderItem
2. Tạo API sales export
3. Cập nhật SecurityConfig

### Tuần tới
4. Implement tự động tạo kỳ
5. Thêm audit log cơ bản

### Tương lai
6. Tích hợp API vận chuyển
7. Dashboard analytics nâng cao
8. Báo cáo thuế tự động
