# Module Kế Toán - Hướng Dẫn Hoàn Chỉnh

## Tổng Quan

Module kế toán được thiết kế để quản lý toàn bộ hoạt động tài chính của hệ thống thương mại điện tử, bao gồm:

- **Giao dịch tài chính**: Tự động ghi nhận thu chi từ đơn hàng
- **Đối soát thanh toán**: So sánh với các cổng thanh toán
- **Quản lý thuế**: VAT và thuế TNDN
- **Báo cáo tài chính**: Lãi lỗ, dòng tiền, phân tích chi phí
- **Quản lý kỳ kế toán**: Chốt sổ và kiểm soát

## Cấu Trúc Backend

### 1. Entities

#### FinancialTransaction
```java
- id: Long (Primary Key)
- transactionCode: String (Mã giao dịch duy nhất)
- orderId: String (Mã đơn hàng liên quan)
- type: TransactionType (REVENUE, EXPENSE, REFUND)
- category: TransactionCategory (SALES, SHIPPING, PAYMENT_FEE, TAX, etc.)
- amount: BigDecimal (Số tiền)
- description: String (Mô tả)
- transactionDate: LocalDateTime
- createdBy: String
- createdAt: LocalDateTime
```

#### TaxReport
```java
- id: Long (Primary Key)
- reportCode: String (Mã báo cáo thuế)
- periodStart/End: LocalDate (Kỳ báo cáo)
- taxType: TaxType (VAT, CORPORATE_TAX)
- taxableRevenue: BigDecimal (Doanh thu chịu thuế)
- taxRate: BigDecimal (Thuế suất %)
- taxAmount: BigDecimal (Số thuế phải nộp)
- paidTax: BigDecimal (Đã nộp)
- remainingTax: BigDecimal (Còn nợ)
- status: TaxReportStatus (DRAFT, SUBMITTED, PAID)
```

### 2. Services

#### FinancialTransactionService
- Quản lý giao dịch tài chính
- Tự động tạo giao dịch từ đơn hàng
- Báo cáo lãi lỗ, dòng tiền
- Phân tích chi phí theo danh mục

#### TaxService
- Quản lý báo cáo thuế VAT và TNDN
- Tính toán thuế tự động
- Theo dõi tình trạng nộp thuế

#### AccountingService (Existing)
- Đối soát thanh toán
- Quản lý kỳ kế toán
- Báo cáo tổng hợp

### 3. Event Listeners

#### OrderEventListener
Tự động tạo giao dịch tài chính khi:
- Đơn hàng được thanh toán → Tạo giao dịch doanh thu
- Đơn hàng bị hủy sau thanh toán → Tạo giao dịch hoàn tiền

## Cấu Trúc Frontend

### 1. Trang Chính (/admin/accounting)
- Dashboard với thống kê tổng quan
- Quick actions đến các module con

### 2. Quản Lý Thuế (/admin/accounting/tax)
- Danh sách báo cáo thuế VAT và TNDN
- Tạo, sửa, nộp báo cáo thuế
- Theo dõi tình trạng thanh toán

### 3. Giao Dịch Tài Chính (/admin/accounting/transactions)
- Danh sách tất cả giao dịch thu chi
- Tìm kiếm theo khoảng thời gian
- Thêm/sửa/xóa giao dịch thủ công

### 4. Báo Cáo Nâng Cao (/admin/accounting/advanced-reports)
- Báo cáo lãi lỗ chi tiết
- Báo cáo dòng tiền
- Phân tích cơ cấu chi phí

### 5. Đối Soát & Quản Lý Kỳ (Existing)
- Đối soát thanh toán với cổng
- Chốt sổ kỳ kế toán

## Cài Đặt và Triển Khai

### 1. Chạy SQL Script
```sql
-- Chạy file accounting_tables.sql để tạo bảng và dữ liệu mẫu
```

### 2. Cấu Hình Quyền
Đảm bảo user có role `ADMIN` hoặc position `ACCOUNTANT` để truy cập module.

### 3. Event Integration
Module tự động lắng nghe sự kiện thay đổi trạng thái đơn hàng để tạo giao dịch tài chính.

## Tính Năng Chính

### 1. Tự Động Hóa
- **Giao dịch từ đơn hàng**: Tự động tạo khi đơn hàng được thanh toán
- **Tính thuế**: Tự động tính VAT và thuế TNDN từ doanh thu
- **Phí dịch vụ**: Tự động tính phí thanh toán (2% doanh thu)

### 2. Báo Cáo Thông Minh
- **Lãi lỗ**: Phân tích chi tiết doanh thu, chi phí, lợi nhuận
- **Dòng tiền**: Theo dõi dòng tiền vào/ra theo hoạt động
- **Chi phí**: Phân tích cơ cấu chi phí theo danh mục

### 3. Quản Lý Thuế
- **VAT**: Tính từ doanh thu bán hàng (10%)
- **Thuế TNDN**: Tính từ lợi nhuận (20%)
- **Theo dõi**: Trạng thái nộp thuế và số còn nợ

### 4. Đối Soát & Kiểm Soát
- **Đối soát thanh toán**: So sánh với dữ liệu cổng thanh toán
- **Chốt kỳ**: Kiểm soát chỉnh sửa sau khi chốt sổ
- **Phân quyền**: Chỉ Admin mới mở khóa kỳ đã chốt

## API Endpoints

### Financial Transactions
```
GET    /api/accounting/transactions              - Danh sách giao dịch
POST   /api/accounting/transactions/search       - Tìm kiếm theo ngày
POST   /api/accounting/transactions              - Tạo giao dịch
PUT    /api/accounting/transactions/{id}         - Cập nhật
DELETE /api/accounting/transactions/{id}         - Xóa
```

### Tax Management
```
GET    /api/accounting/tax/reports               - Tất cả báo cáo thuế
GET    /api/accounting/tax/reports/{type}        - Theo loại thuế
POST   /api/accounting/tax/reports              - Tạo báo cáo
PUT    /api/accounting/tax/reports/{id}         - Cập nhật
POST   /api/accounting/tax/reports/{id}/submit  - Nộp báo cáo
POST   /api/accounting/tax/reports/{id}/mark-paid - Đánh dấu đã nộp
GET    /api/accounting/tax/summary              - Tổng quan thuế
```

### Advanced Reports
```
POST   /api/accounting/reports/profit-loss      - Báo cáo lãi lỗ
POST   /api/accounting/reports/cash-flow        - Báo cáo dòng tiền
POST   /api/accounting/reports/expense-analysis - Phân tích chi phí
```

## Quy Trình Sử Dụng

### 1. Thiết Lập Ban Đầu
1. Chạy SQL script tạo bảng
2. Cấu hình quyền user
3. Kiểm tra event listener hoạt động

### 2. Sử Dụng Hàng Ngày
1. **Giao dịch tự động**: Hệ thống tự tạo khi có đơn hàng
2. **Thêm chi phí thủ công**: Marketing, vận hành, etc.
3. **Đối soát thanh toán**: Import file từ cổng thanh toán
4. **Xem báo cáo**: Theo dõi tình hình tài chính

### 3. Cuối Kỳ
1. **Tạo báo cáo thuế**: VAT hàng tháng, TNDN hàng quý
2. **Đối soát tổng thể**: Kiểm tra sai lệch
3. **Chốt kỳ**: Khóa dữ liệu không cho sửa
4. **Xuất báo cáo**: Excel cho kế toán trưởng

## Lưu Ý Quan Trọng

### 1. Bảo Mật
- Chỉ ADMIN và ACCOUNTANT được truy cập
- Dữ liệu tài chính được mã hóa
- Log đầy đủ các thao tác

### 2. Hiệu Suất
- Index được tối ưu cho truy vấn theo ngày
- Phân trang cho danh sách lớn
- Cache cho báo cáo thường xuyên

### 3. Tích Hợp
- Event-driven với module đơn hàng
- API chuẩn RESTful
- Hỗ trợ xuất Excel

### 4. Mở Rộng
- Dễ dàng thêm loại thuế mới
- Linh hoạt thêm danh mục chi phí
- Tích hợp với hệ thống kế toán bên ngoài

## Troubleshooting

### 1. Giao Dịch Không Tự Tạo
- Kiểm tra OrderEventListener đã được đăng ký
- Xem log để debug event firing
- Đảm bảo transaction không bị rollback

### 2. Báo Cáo Sai Số Liệu
- Kiểm tra khoảng thời gian truy vấn
- Xác nhận dữ liệu giao dịch đầy đủ
- So sánh với dữ liệu đơn hàng gốc

### 3. Lỗi Quyền Truy Cập
- Kiểm tra role và position của user
- Xem cấu hình @PreAuthorize
- Đảm bảo JWT token hợp lệ

Module kế toán này cung cấp giải pháp toàn diện cho quản lý tài chính của hệ thống thương mại điện tử, từ ghi nhận giao dịch tự động đến báo cáo phân tích chi tiết.