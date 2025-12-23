# 📊 Tài liệu Implementation Các Module Kế toán

## ✅ Đã hoàn thành

### 1. **Giao dịch tài chính (Financial Transactions)**
- **Entity**: `FinancialTransaction`
- **Enums**: `TransactionType`, `TransactionCategory`
- **Repository**: `FinancialTransactionRepository`
- **Service**: `FinancialTransactionService` + Implementation
- **Controller**: `FinancialTransactionController`
- **API Endpoints**:
  - `GET /api/accounting/transactions` - Lấy danh sách giao dịch (phân trang)
  - `GET /api/accounting/transactions/{id}` - Chi tiết giao dịch
  - `POST /api/accounting/transactions` - Tạo giao dịch mới
  - `PUT /api/accounting/transactions/{id}` - Cập nhật giao dịch
  - `DELETE /api/accounting/transactions/{id}` - Xóa giao dịch
  - `POST /api/accounting/transactions/search` - Tìm kiếm theo ngày
- **Security**: Chỉ ADMIN và ACCOUNTANT

### 2. **Kỳ kế toán (Accounting Periods)**
- **Entity**: `AccountingPeriod`
- **Enum**: `PeriodStatus` (OPEN, CLOSED)
- **Repository**: `AccountingPeriodRepository`
- **Service**: `AccountingPeriodService` + Implementation
- **Controller**: `AccountingPeriodController`
- **API Endpoints**:
  - `GET /api/accounting/periods` - Lấy danh sách kỳ
  - `GET /api/accounting/periods/{id}` - Chi tiết kỳ
  - `POST /api/accounting/periods` - Tạo kỳ mới
  - `POST /api/accounting/periods/{id}/close` - Chốt kỳ
  - `POST /api/accounting/periods/{id}/reopen` - Mở khóa kỳ (ADMIN only)
  - `POST /api/accounting/periods/{id}/calculate` - Tính toán thống kê
- **Security**: ADMIN và ACCOUNTANT (reopen chỉ ADMIN)
- **Features**:
  - Tự động tính doanh thu, chi phí, lợi nhuận
  - Tính tỷ lệ sai lệch
  - Không cho chốt khi sai số > 15%

### 3. **Thuế (Tax Management)**
- **Entity**: `TaxReport`
- **Enums**: `TaxType` (VAT, CORPORATE_TAX), `TaxStatus` (DRAFT, SUBMITTED, PAID)
- **Repository**: `TaxReportRepository`
- **Service**: `TaxReportService` + Implementation
- **Controller**: `TaxReportController`
- **API Endpoints**:
  - `GET /api/accounting/tax/reports` - Lấy tất cả báo cáo thuế
  - `GET /api/accounting/tax/reports/{type}` - Lấy theo loại thuế
  - `GET /api/accounting/tax/reports/detail/{id}` - Chi tiết báo cáo
  - `POST /api/accounting/tax/reports` - Tạo báo cáo thuế
  - `PUT /api/accounting/tax/reports/{id}` - Cập nhật báo cáo
  - `POST /api/accounting/tax/reports/{id}/submit` - Gửi báo cáo
  - `POST /api/accounting/tax/reports/{id}/mark-paid` - Đánh dấu đã nộp
  - `GET /api/accounting/tax/summary` - Tổng quan thuế
- **Security**: Chỉ ADMIN và ACCOUNTANT
- **Features**:
  - Tự động tính số thuế phải nộp
  - Theo dõi VAT (10%) và thuế TNDN (20%)
  - Tổng hợp số thuế còn nợ và đã nộp

### 4. **Security Service**
- **Service**: `EmployeeSecurityService`
- **Method**: `hasPosition(Authentication, String)` - Kiểm tra position của employee
- **Usage**: Dùng trong `@PreAuthorize` để phân quyền theo position

## 🗄️ Database Schema

Hibernate sẽ tự động tạo các bảng sau khi restart backend:

### `financial_transactions`
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- transaction_code (VARCHAR, UNIQUE)
- type (VARCHAR) - REVENUE, EXPENSE, REFUND
- category (VARCHAR) - SALES, SHIPPING, PAYMENT_FEE, TAX, etc.
- amount (DOUBLE)
- order_id (BIGINT, nullable)
- supplier_id (BIGINT, nullable)
- description (VARCHAR)
- transaction_date (DATETIME)
- created_at (DATETIME)
- created_by (VARCHAR)
```

### `accounting_periods`
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- name (VARCHAR)
- start_date (DATE)
- end_date (DATE)
- status (VARCHAR) - OPEN, CLOSED
- total_revenue (DOUBLE)
- total_expense (DOUBLE)
- net_profit (DOUBLE)
- discrepancy_rate (DOUBLE)
- closed_at (DATETIME)
- closed_by (VARCHAR)
- created_at (DATETIME)
```

### `tax_reports`
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- report_code (VARCHAR, UNIQUE)
- tax_type (VARCHAR) - VAT, CORPORATE_TAX
- period_start (DATE)
- period_end (DATE)
- taxable_revenue (DOUBLE)
- tax_rate (DOUBLE)
- tax_amount (DOUBLE)
- paid_amount (DOUBLE)
- remaining_tax (DOUBLE)
- status (VARCHAR) - DRAFT, SUBMITTED, PAID
- submitted_at (DATETIME)
- paid_at (DATETIME)
- created_at (DATETIME)
- created_by (VARCHAR)
```

## 🔐 Phân quyền

Tất cả API kế toán đều yêu cầu:
- **ADMIN**: Có quyền truy cập tất cả
- **ACCOUNTANT**: Có quyền truy cập tất cả (trừ reopen period)
- **Nhân viên khác**: KHÔNG có quyền truy cập

### 5. **Báo cáo nâng cao (Advanced Reports)** - ✅ DONE
- **DTOs**: `AdvancedReportRequest`, `AdvancedReportResponse`
- **Service**: `AdvancedReportService` + Implementation
- **Controller**: `AdvancedReportController`
- **API Endpoints**:
  - `POST /api/accounting/reports/profit-loss` - Báo cáo lãi lỗ
  - `POST /api/accounting/reports/cash-flow` - Báo cáo dòng tiền
  - `POST /api/accounting/reports/expense-analysis` - Phân tích chi phí
- **Security**: Chỉ ADMIN và ACCOUNTANT
- **Features**:
  - Báo cáo lãi lỗ: Doanh thu, chi phí, lợi nhuận gộp/ròng, VAT, thuế TNDN
  - Báo cáo dòng tiền: Hoạt động kinh doanh, đầu tư, tài chính
  - Phân tích chi phí: Phân tích theo danh mục với tỷ lệ phần trăm
- **Frontend**: Đã có UI tại `/admin/accounting/advanced-reports` và `/employee/accounting/advanced-reports`

### 6. **Đối soát vận chuyển (Shipping Reconciliation)** - ✅ DONE
- **DTO**: `ShippingReconciliationResponse`
- **Service**: `ShippingReconciliationService` + Implementation
- **Controller**: `ShippingReconciliationController`
- **API Endpoints**:
  - `GET /api/accounting/shipping-reconciliation?startDate=&endDate=` - Đối soát vận chuyển
- **Security**: Chỉ ADMIN và ACCOUNTANT
- **Features**:
  - So sánh phí vận chuyển thu từ khách vs chi phí thực tế (80% phí thu)
  - Tính lợi nhuận vận chuyển và tỷ suất lợi nhuận
  - Chi tiết từng đơn hàng với địa chỉ giao hàng
- **Frontend**: Đã có UI tại `/admin/accounting/shipping` và `/employee/accounting/shipping`

## 🚀 Cách sử dụng

1. **Restart backend** để Hibernate tạo bảng
2. **Test API** bằng Postman hoặc frontend
3. **Kiểm tra database** xem bảng đã được tạo chưa
4. **Tạo dữ liệu mẫu** để test

## 📌 Lưu ý

- Tất cả entity đều có `@PrePersist` để tự động set `createdAt` và generate code
- Security được implement bằng `@PreAuthorize` với `EmployeeSecurityService`
- Frontend đã có sẵn UI, chỉ cần kết nối API
- Hibernate auto-ddl sẽ tự động tạo bảng, không cần chạy SQL script
