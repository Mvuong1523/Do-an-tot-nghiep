# 📊 Hướng dẫn Module Kế toán & Đối soát

## ✅ Đã hoàn thành

### 1. **Backend Implementation**

#### Entities
- `PaymentReconciliation` - Lưu dữ liệu đối soát thanh toán
- `AccountingPeriod` - Quản lý kỳ báo cáo kế toán
- `ReconciliationStatus` - Trạng thái đối soát (MATCHED, MISMATCHED, MISSING)
- `PeriodStatus` - Trạng thái kỳ (OPEN, CLOSED)

#### Repositories
- `PaymentReconciliationRepository` - Query đối soát
- `AccountingPeriodRepository` - Query kỳ báo cáo
- Đã bổ sung query methods cho `OrderRepository` và `PaymentRepository`

#### Services
- `AccountingService` - Interface
- `AccountingServiceImpl` - Implementation đầy đủ
- `ExcelExportService` - Xuất báo cáo Excel

#### Controller
- `AccountingController` - REST API endpoints
- Security: Chỉ ADMIN và ACCOUNTANT có quyền truy cập
- Riêng reopen period chỉ ADMIN

### 2. **Tính năng chính**

#### Dashboard Stats
```
GET /api/accounting/stats
```
- Tổng doanh thu (30 ngày gần nhất)
- Số lượng đối soát pending/completed
- Tổng sai lệch

#### Đối soát thanh toán
```
POST /api/accounting/payment-reconciliation
Body: {
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "gateway": "SEPAY" // hoặc "ALL"
}
```
- So sánh dữ liệu hệ thống vs cổng thanh toán
- Tính toán sai lệch tự động
- Cảnh báo sai lệch >5 triệu

#### Import file đối soát
```
POST /api/accounting/payment-reconciliation/import
Params: file (CSV), gateway (SEPAY/VNPAY)
```
Format CSV:
```
OrderCode,TransactionId,Amount,TransactionDate
ORD001,TXN123,1000000,2024-01-15T10:30:00
```

#### Báo cáo tài chính
```
GET /api/accounting/reports?startDate=2024-01-01&endDate=2024-01-31&viewMode=ORDERS
```
ViewMode:
- `ORDERS` - Chi tiết từng đơn hàng
- `DAILY` - Tổng hợp theo ngày
- `MONTHLY` - Tổng hợp theo tháng

Báo cáo bao gồm:
- Doanh thu
- VAT (10%)
- Giá vốn (60% subtotal)
- Phí vận chuyển
- Phí cổng thanh toán (2%)
- Lợi nhuận gộp
- Thuế TNDN (20%)
- Lợi nhuận ròng
- Thực nhận

#### Xuất Excel
```
GET /api/accounting/reports/export?startDate=2024-01-01&endDate=2024-01-31
```
Response:
```json
{
  "fileName": "BaoCaoTaiChinh_2024-01-01_2024-01-31.xlsx",
  "data": "base64_encoded_excel_data"
}
```

#### Quản lý kỳ báo cáo
```
GET /api/accounting/periods
POST /api/accounting/periods/{id}/close
POST /api/accounting/periods/{id}/reopen (ADMIN only)
```

Chốt kỳ:
- Kiểm tra sai số <15%
- Lưu thông tin người chốt và thời gian
- Không cho phép chốt nếu sai số >15%

Mở khóa kỳ:
- Chỉ ADMIN
- Reset trạng thái về OPEN

### 3. **Security & Authentication**

#### SecurityUtils
```java
SecurityUtils.getCurrentUserEmail() // Lấy email user hiện tại
SecurityUtils.isAdmin() // Kiểm tra quyền Admin
SecurityUtils.hasRole("ACCOUNTANT") // Kiểm tra role
```

#### Controller Security
```java
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
```

### 4. **Integration với modules khác**

#### Order Module
- Query đơn hàng theo khoảng thời gian
- Tính tổng doanh thu
- Lấy chi tiết đơn hàng cho báo cáo

#### Payment Module
- Query thanh toán thành công
- Tính tổng số tiền thanh toán
- Đối soát với gateway

### 5. **Excel Export**

Dependencies đã thêm:
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

Features:
- Header với style (màu xanh đậm, chữ trắng, bold)
- Format số với dấu phẩy ngăn cách
- Auto-size columns
- Export base64 để frontend download

## 🔧 Cấu hình cần thiết

### Database
Đã có migration scripts trong project:
- `PaymentReconciliation` table
- `AccountingPeriod` table

### Security Config
Thêm vào `SecurityConfig.java`:
```java
.requestMatchers("/api/accounting/**").hasAnyAuthority("ADMIN", "ACCOUNTANT")
```

### Role/Position
Cần thêm role `ACCOUNTANT` vào enum `Position`:
```java
public enum Position {
    ADMIN,
    ACCOUNTANT,  // Thêm role này
    WAREHOUSE,
    PRODUCT_MANAGER,
    SALES
}
```

## 📝 Testing

### Test với Postman/HTTP Client

1. **Login as Admin/Accountant**
```http
POST /api/auth/login
{
  "email": "admin@example.com",
  "password": "password"
}
```

2. **Get Stats**
```http
GET /api/accounting/stats
Authorization: Bearer {token}
```

3. **Get Financial Reports**
```http
GET /api/accounting/reports?startDate=2024-01-01&endDate=2024-01-31&viewMode=ORDERS
Authorization: Bearer {token}
```

4. **Export Excel**
```http
GET /api/accounting/reports/export?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {token}
```

Frontend decode base64:
```javascript
const blob = new Blob([
  Uint8Array.from(atob(response.data.data), c => c.charCodeAt(0))
], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

const url = window.URL.createObjectURL(blob);
const a = document.createElement('a');
a.href = url;
a.download = response.data.fileName;
a.click();
```

## 🎯 Next Steps (Tùy chọn)

1. **Tích hợp Payment Gateway API**
   - Tự động pull dữ liệu từ VNPay/MoMo
   - Scheduled job đối soát hàng ngày

2. **Advanced Reports**
   - Biểu đồ doanh thu
   - Phân tích xu hướng
   - So sánh theo kỳ

3. **Notification**
   - Email cảnh báo sai lệch
   - Thông báo khi cần chốt kỳ

4. **Audit Log**
   - Lưu lịch sử thay đổi
   - Track user actions

## ✨ Summary

Module Kế toán & Đối soát đã hoàn thành đầy đủ:
- ✅ Backend API đầy đủ
- ✅ Authentication & Authorization
- ✅ Integration với Order & Payment
- ✅ Excel Export
- ✅ Financial Reports với tính toán chi tiết
- ✅ Period Management với validation

Có thể test ngay với frontend đã có!
