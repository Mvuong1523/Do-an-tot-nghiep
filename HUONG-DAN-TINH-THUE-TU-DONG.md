# Hướng Dẫn Sử Dụng Tính Thuế Tự Động

## ✅ Tính Năng Đã Hoàn Thành

Hệ thống giờ có thể **tự động tính doanh thu chịu thuế** từ dữ liệu thực tế trong bảng `financial_transactions`.

---

## 🎯 Cách Sử Dụng

### Bước 1: Truy Cập Trang Quản Lý Thuế

1. Đăng nhập với tài khoản ACCOUNTANT: `ketoan@gmail.com`
2. Truy cập: http://localhost:3000/employee/accounting/tax

### Bước 2: Tạo Báo Cáo Thuế Mới

1. Click nút **"Tạo báo cáo thuế"**
2. Chọn **Loại thuế**:
   - **VAT**: Thuế giá trị gia tăng (10% trên doanh thu)
   - **Thuế TNDN**: Thuế thu nhập doanh nghiệp (20% trên lợi nhuận)

### Bước 3: Chọn Kỳ Báo Cáo

1. Chọn **Từ ngày**: Ví dụ `2025-12-01`
2. Chọn **Đến ngày**: Ví dụ `2025-12-31`

### Bước 4: Tính Toán Tự Động

1. Click nút **"🔄 Tính toán tự động"** bên cạnh trường "Doanh thu chịu thuế"
2. Hệ thống sẽ:
   - Lấy dữ liệu từ `financial_transactions`
   - Tính tổng doanh thu (REVENUE)
   - Tính tổng chi phí (EXPENSE)
   - Tính lợi nhuận (doanh thu - chi phí)
   - Tự động điền vào trường "Doanh thu chịu thuế"

### Bước 5: Kiểm Tra và Tạo Báo Cáo

1. Kiểm tra số liệu đã được điền tự động
2. Xem số thuế phải nộp (tự động tính)
3. Click **"Tạo báo cáo"**

---

## 📊 Công Thức Tính

### Thuế VAT (10%)
```
Doanh thu chịu thuế = Tổng doanh thu từ bán hàng
Số thuế VAT = Doanh thu × 10%
```

**Ví dụ:**
- Doanh thu tháng 12: 100,000,000 ₫
- Thuế VAT phải nộp: 10,000,000 ₫

### Thuế TNDN (20%)
```
Doanh thu chịu thuế = Lợi nhuận (Doanh thu - Chi phí)
Số thuế TNDN = Lợi nhuận × 20%
```

**Ví dụ:**
- Doanh thu tháng 12: 100,000,000 ₫
- Chi phí tháng 12: 50,000,000 ₫
- Lợi nhuận: 50,000,000 ₫
- Thuế TNDN phải nộp: 10,000,000 ₫

---

## 🔍 Kiểm Tra Dữ Liệu Nguồn

Để xem dữ liệu doanh thu và chi phí, chạy query:

```sql
SELECT 
    type,
    SUM(amount) as total,
    COUNT(*) as count
FROM financial_transactions
WHERE transaction_date BETWEEN '2025-12-01' AND '2025-12-31'
GROUP BY type;
```

**Kết quả mẫu:**
```
type     | total       | count
---------|-------------|------
REVENUE  | 100,000,000 | 5
EXPENSE  | 50,000,000  | 3
```

---

## 🎨 Giao Diện Mới

### Trong Modal Tạo Báo Cáo:

```
┌─────────────────────────────────────────────┐
│ Doanh thu chịu thuế *    🔄 Tính toán tự động│
├─────────────────────────────────────────────┤
│ [___________________]                        │
│ Số thuế phải nộp: 10,000,000 ₫              │
└─────────────────────────────────────────────┘
```

### Thông Báo Khi Tính Toán:

- ✅ **Thành công**: "Doanh thu chịu thuế VAT: 100,000,000 ₫"
- ✅ **Thành công**: "Lợi nhuận chịu thuế TNDN: 50,000,000 ₫"
- ❌ **Lỗi**: "Vui lòng chọn kỳ báo cáo trước"

---

## 🚀 Quy Trình Hoàn Chỉnh

### 1. Tạo Báo Cáo (DRAFT)
- Sử dụng "Tính toán tự động" để lấy dữ liệu
- Hoặc nhập thủ công
- Click "Tạo báo cáo"

### 2. Nộp Báo Cáo (SUBMITTED)
- Kiểm tra lại số liệu
- Click biểu tượng ✓ (check) để nộp báo cáo

### 3. Đánh Dấu Đã Thanh Toán (PAID)
- Sau khi nộp thuế thực tế
- Click biểu tượng 💵 (dollar) để đánh dấu đã thanh toán

---

## 📝 Lưu Ý Quan Trọng

### ✅ Ưu Điểm:
- Tiết kiệm thời gian nhập liệu
- Giảm sai sót do nhập tay
- Dữ liệu chính xác từ hệ thống
- Tự động cập nhật theo doanh thu thực tế

### ⚠️ Lưu Ý:
- Cần có dữ liệu trong `financial_transactions`
- Chọn đúng kỳ báo cáo
- Kiểm tra số liệu trước khi tạo báo cáo
- Không thể sửa báo cáo đã nộp (SUBMITTED)

### 🔧 Nếu Không Có Dữ Liệu:
1. Kiểm tra bảng `financial_transactions`
2. Đảm bảo có giao dịch trong kỳ báo cáo
3. Kiểm tra trường `type` (REVENUE/EXPENSE)
4. Kiểm tra trường `transaction_date`

---

## 🧪 Test Nhanh

### Test 1: Tính VAT Tháng 12/2025
1. Loại thuế: VAT
2. Từ ngày: 2025-12-01
3. Đến ngày: 2025-12-31
4. Click "🔄 Tính toán tự động"
5. Kiểm tra số liệu

### Test 2: Tính Thuế TNDN Tháng 12/2025
1. Loại thuế: Thuế TNDN (20%)
2. Từ ngày: 2025-12-01
3. Đến ngày: 2025-12-31
4. Click "🔄 Tính toán tự động"
5. Kiểm tra số liệu

---

## 📞 Troubleshooting

### Lỗi: "Vui lòng chọn kỳ báo cáo trước"
**Nguyên nhân**: Chưa chọn "Từ ngày" và "Đến ngày"
**Giải pháp**: Chọn kỳ báo cáo trước khi click "Tính toán tự động"

### Lỗi: "Lỗi khi tính toán doanh thu"
**Nguyên nhân**: Lỗi kết nối API hoặc không có dữ liệu
**Giải pháp**: 
1. Kiểm tra backend đang chạy (port 8080)
2. Kiểm tra token đăng nhập
3. Kiểm tra dữ liệu trong `financial_transactions`

### Số Liệu Bằng 0
**Nguyên nhân**: Không có giao dịch trong kỳ báo cáo
**Giải pháp**: 
1. Kiểm tra kỳ báo cáo đã chọn
2. Kiểm tra dữ liệu trong database
3. Thêm dữ liệu mẫu nếu cần

---

## 🎉 Hoàn Thành!

Tính năng tính thuế tự động đã sẵn sàng sử dụng. Hãy thử ngay!

**Các file liên quan:**
- `src/frontend/app/employee/accounting/tax/page.tsx` - Giao diện frontend
- `src/main/java/com/doan/WEB_TMDT/module/accounting/controller/TaxReportController.java` - API endpoints
- `src/main/java/com/doan/WEB_TMDT/module/accounting/service/impl/TaxReportServiceImpl.java` - Logic tính toán
- `TAX-AUTO-CALCULATION-GUIDE.md` - Tài liệu kỹ thuật
- `TEST-TAX-AUTO-CALCULATION.md` - Test cases
- `create-tax-from-revenue.http` - Test API

**Restart frontend để áp dụng thay đổi:**
```bash
# Trong terminal frontend
Ctrl + C
npm run dev
```
