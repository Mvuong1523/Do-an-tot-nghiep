# 🚀 Quick Start - Module Kế toán

## Bước 1: Restart Backend

```bash
# Dừng backend nếu đang chạy (Ctrl+C)
# Sau đó khởi động lại:
mvn spring-boot:run
```

Hibernate sẽ tự động tạo 3 bảng mới:
- `financial_transactions`
- `accounting_periods`
- `tax_reports`

## Bước 2: Kiểm tra Database

```sql
USE web3;
SHOW TABLES;

-- Kiểm tra cấu trúc bảng
DESCRIBE financial_transactions;
DESCRIBE accounting_periods;
DESCRIBE tax_reports;
```

## Bước 3: Test với Frontend

### Đăng nhập
1. Mở trình duyệt: http://localhost:3000
2. Đăng nhập với tài khoản:
   - **Admin**: username/password của admin
   - **Accountant**: username/password của nhân viên kế toán

### Truy cập các trang kế toán

#### Nếu là Admin:
- Giao dịch: http://localhost:3000/admin/accounting/transactions
- Kỳ kế toán: http://localhost:3000/admin/accounting/periods
- Thuế: http://localhost:3000/admin/accounting/tax
- Báo cáo nâng cao: http://localhost:3000/admin/accounting/advanced-reports
- Đối soát vận chuyển: http://localhost:3000/admin/accounting/shipping

#### Nếu là Accountant:
- Giao dịch: http://localhost:3000/employee/accounting/transactions
- Kỳ kế toán: http://localhost:3000/employee/accounting/periods
- Thuế: http://localhost:3000/employee/accounting/tax
- Báo cáo nâng cao: http://localhost:3000/employee/accounting/advanced-reports
- Đối soát vận chuyển: http://localhost:3000/employee/accounting/shipping

## Bước 4: Tạo dữ liệu mẫu

### 1. Tạo giao dịch tài chính
- Vào trang "Giao dịch tài chính"
- Click "Thêm giao dịch"
- Nhập thông tin:
  - Loại: REVENUE (Doanh thu) hoặc EXPENSE (Chi phí)
  - Danh mục: SALES, SHIPPING, PAYMENT_FEE, TAX, etc.
  - Số tiền
  - Mô tả
  - Ngày giao dịch
- Click "Lưu"

### 2. Tạo kỳ kế toán
- Vào trang "Kỳ kế toán"
- Click "Tạo kỳ mới"
- Nhập:
  - Tên kỳ: "Tháng 12/2024"
  - Ngày bắt đầu: 01/12/2024
  - Ngày kết thúc: 31/12/2024
- Click "Tạo kỳ"
- Sau khi có giao dịch, click "Tính toán" để cập nhật số liệu
- Click "Chốt kỳ" khi đã kiểm tra xong

### 3. Tạo báo cáo thuế
- Vào trang "Thuế"
- Click "Tạo báo cáo thuế"
- Chọn:
  - Loại thuế: VAT hoặc CORPORATE_TAX
  - Kỳ báo cáo: từ ngày - đến ngày
  - Doanh thu chịu thuế
- Hệ thống tự động tính số thuế
- Click "Lưu"

### 4. Xem báo cáo nâng cao
- Vào trang "Báo cáo nâng cao"
- Chọn loại báo cáo:
  - Báo cáo lãi lỗ
  - Báo cáo dòng tiền
  - Phân tích chi phí
- Chọn khoảng thời gian
- Click "Tạo báo cáo"

### 5. Đối soát vận chuyển
- Vào trang "Đối soát vận chuyển"
- Chọn khoảng thời gian
- Click "Tải dữ liệu"
- Xem chi tiết lợi nhuận vận chuyển

## Bước 5: Test API với Postman (Optional)

### 1. Lấy Token
```
POST http://localhost:8080/api/auth/login
Body: {
  "username": "admin",
  "password": "password"
}
```

### 2. Test API
Thêm header: `Authorization: Bearer <token>`

#### Giao dịch tài chính
```
GET  http://localhost:8080/api/accounting/transactions?page=0&size=10
POST http://localhost:8080/api/accounting/transactions
```

#### Kỳ kế toán
```
GET  http://localhost:8080/api/accounting/periods
POST http://localhost:8080/api/accounting/periods
```

#### Thuế
```
GET  http://localhost:8080/api/accounting/tax/reports
POST http://localhost:8080/api/accounting/tax/reports
```

#### Báo cáo nâng cao
```
POST http://localhost:8080/api/accounting/reports/profit-loss
Body: {
  "startDate": "2024-12-01",
  "endDate": "2024-12-31",
  "groupBy": "MONTHLY"
}
```

#### Đối soát vận chuyển
```
GET http://localhost:8080/api/accounting/shipping-reconciliation?startDate=2024-12-01&endDate=2024-12-31
```

## ⚠️ Lưu ý

1. **Phân quyền**: Chỉ ADMIN và ACCOUNTANT mới truy cập được các API kế toán
2. **Database**: Hibernate tự động tạo bảng, không cần chạy SQL script
3. **Dữ liệu**: Cần có đơn hàng đã thanh toán để test đối soát vận chuyển
4. **Báo cáo**: Cần có giao dịch tài chính để tạo báo cáo nâng cao

## 🐛 Troubleshooting

### Backend không khởi động
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra cấu hình database trong `application.properties`
- Xem log để tìm lỗi

### Frontend không hiển thị dữ liệu
- Kiểm tra backend đã chạy chưa (http://localhost:8080)
- Kiểm tra đã đăng nhập chưa
- Mở Developer Console (F12) để xem lỗi

### API trả về 403 Forbidden
- Kiểm tra token còn hạn không
- Kiểm tra user có role ADMIN hoặc position ACCOUNTANT không
- Đăng nhập lại để lấy token mới

### Không thấy bảng trong database
- Restart backend để Hibernate tạo bảng
- Kiểm tra `spring.jpa.hibernate.ddl-auto=update` trong application.properties
- Xem log backend khi khởi động

## ✅ Hoàn thành!

Bây giờ bạn đã có hệ thống kế toán hoàn chỉnh với:
- ✅ 6 module kế toán đầy đủ
- ✅ Backend API bảo mật
- ✅ Frontend UI thân thiện
- ✅ Database tự động tạo
- ✅ Phân quyền chặt chẽ

Chúc bạn sử dụng hiệu quả! 🎉
