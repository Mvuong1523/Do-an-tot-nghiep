# Sửa Lỗi Quyền Truy Cập Giao Dịch Kế Toán

## 🐛 Lỗi

User ACCOUNTANT (ketoan@gmail.com) gặp lỗi 403 Forbidden khi:
1. Truy cập `/api/accounting/transactions` - Trang giao dịch kế toán
2. Truy cập `/api/dashboard/stats` - Thông báo dashboard

## 🔍 Nguyên Nhân

**FinancialTransactionController** sử dụng:
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
```

Nhưng JWT token của ACCOUNTANT chỉ có authorities:
- `ACCOUNTANT`
- `EMPLOYEE`  
- `ROLE_EMPLOYEE`

Không có `ROLE_ADMIN` nên expression phức tạp này fail.

## ✅ Giải Pháp

Đổi tất cả endpoints trong `FinancialTransactionController` sang:
```java
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
```

## 📝 File Đã Sửa

**File:** `src/main/java/com/doan/WEB_TMDT/module/accounting/controller/FinancialTransactionController.java`

**Các endpoints đã sửa:**
1. `GET /api/accounting/transactions` - Lấy danh sách giao dịch
2. `GET /api/accounting/transactions/{id}` - Lấy chi tiết giao dịch
3. `POST /api/accounting/transactions` - Tạo giao dịch mới
4. `PUT /api/accounting/transactions/{id}` - Cập nhật giao dịch
5. `DELETE /api/accounting/transactions/{id}` - Xóa giao dịch
6. `POST /api/accounting/transactions/search` - Tìm kiếm giao dịch

**Trước:**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
```

**Sau:**
```java
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
```

## 🚀 Cần Restart Backend

### Cách 1: Trong IntelliJ IDEA
1. Dừng backend (Stop button hoặc Ctrl+F2)
2. Chạy lại application

### Cách 2: Trong Terminal
```bash
# Dừng process hiện tại (Ctrl+C)
# Chạy lại
mvn spring-boot:run
```

### Cách 3: Kill process và restart
```bash
# Tìm process
netstat -ano | findstr ":8080"

# Kill process (thay PID)
taskkill /PID <PID> /F

# Chạy lại
mvn spring-boot:run
```

## 🧪 Test Sau Khi Restart

### Test 1: Truy cập trang giao dịch
1. Đăng nhập: ketoan@gmail.com
2. Vào: http://localhost:3000/employee/accounting/transactions
3. Kiểm tra: Danh sách giao dịch hiển thị

### Test 2: Tạo giao dịch mới
1. Click "Tạo giao dịch"
2. Điền thông tin
3. Lưu
4. Kiểm tra: Tạo thành công

### Test 3: API trực tiếp
```http
GET http://localhost:8080/api/accounting/transactions?page=0&size=20
Authorization: Bearer {{token}}
```

**Kết quả mong đợi:** 200 OK với danh sách giao dịch

## 📋 Checklist

- [x] Sửa FinancialTransactionController
- [ ] Restart backend
- [ ] Test trang giao dịch
- [ ] Test tạo giao dịch mới
- [ ] Test API trực tiếp

## 🔗 Liên Quan

Lỗi tương tự đã sửa trước đó:
- `DashboardController` - Đã sửa
- `TaxReportController` - Đã sửa
- `FinancialTransactionController` - **Mới sửa**

## 📝 Lưu Ý

### Pattern đúng cho ACCOUNTANT:
```java
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
```

### Pattern SAI (không dùng):
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @employeeSecurityService.hasPosition(authentication, 'ACCOUNTANT'))")
```

### Lý do:
- JWT token có authority là `ACCOUNTANT` (không có prefix `ROLE_`)
- `hasAnyAuthority` check trực tiếp authority name
- `hasRole` thêm prefix `ROLE_` nên `hasRole('ADMIN')` check `ROLE_ADMIN`
- Expression phức tạp với `@employeeSecurityService` dễ fail

## ✅ Kết Luận

Sau khi restart backend, ACCOUNTANT sẽ truy cập được:
- ✅ Trang giao dịch kế toán
- ✅ Tạo/sửa/xóa giao dịch
- ✅ Tìm kiếm giao dịch
- ✅ Dashboard stats (đã sửa trước đó)
- ✅ Báo cáo thuế (đã sửa trước đó)

**Hãy restart backend ngay!** 🚀
