# Test Tính Thuế Tự Động

## Mục Đích
Kiểm tra tính năng tính toán thuế tự động từ dữ liệu doanh thu thực tế.

## Điều Kiện Tiên Quyết
1. Backend đang chạy (port 8080)
2. Frontend đang chạy (port 3000)
3. Đã đăng nhập với tài khoản ACCOUNTANT (ketoan@gmail.com)
4. Có dữ liệu trong bảng `financial_transactions`

## Test Case 1: Tính Toán Tự Động Trong Modal

### Các Bước:
1. Truy cập trang Quản lý thuế: http://localhost:3000/employee/accounting/tax
2. Click nút "Tạo báo cáo thuế"
3. Chọn loại thuế: VAT
4. Chọn kỳ báo cáo:
   - Từ ngày: 2025-12-01
   - Đến ngày: 2025-12-31
5. Click nút "🔄 Tính toán tự động" bên cạnh trường "Doanh thu chịu thuế"

### Kết Quả Mong Đợi:
- ✅ Hiển thị toast thông báo: "Doanh thu chịu thuế VAT: XXX ₫"
- ✅ Trường "Doanh thu chịu thuế" tự động điền số liệu
- ✅ Số thuế phải nộp tự động tính toán (10% của doanh thu)

### Kết Quả Thực Tế:
- [ ] Pass
- [ ] Fail - Lý do: _______________

---

## Test Case 2: Tính Toán Thuế TNDN

### Các Bước:
1. Trong modal tạo báo cáo
2. Chọn loại thuế: Thuế TNDN (20%)
3. Chọn kỳ báo cáo:
   - Từ ngày: 2025-12-01
   - Đến ngày: 2025-12-31
4. Click nút "🔄 Tính toán tự động"

### Kết Quả Mong Đợi:
- ✅ Hiển thị toast thông báo: "Lợi nhuận chịu thuế TNDN: XXX ₫"
- ✅ Trường "Doanh thu chịu thuế" tự động điền lợi nhuận (doanh thu - chi phí)
- ✅ Số thuế phải nộp tự động tính toán (20% của lợi nhuận)

### Kết Quả Thực Tế:
- [ ] Pass
- [ ] Fail - Lý do: _______________

---

## Test Case 3: Validation Khi Chưa Chọn Kỳ

### Các Bước:
1. Trong modal tạo báo cáo
2. KHÔNG chọn "Từ ngày" và "Đến ngày"
3. Click nút "🔄 Tính toán tự động"

### Kết Quả Mong Đợi:
- ✅ Hiển thị toast lỗi: "Vui lòng chọn kỳ báo cáo trước"
- ✅ Nút "Tính toán tự động" bị disable khi chưa chọn kỳ

### Kết Quả Thực Tế:
- [ ] Pass
- [ ] Fail - Lý do: _______________

---

## Test Case 4: Tạo Báo Cáo Với Dữ Liệu Tự Động

### Các Bước:
1. Trong modal tạo báo cáo
2. Chọn loại thuế: VAT
3. Chọn kỳ: 2025-12-01 đến 2025-12-31
4. Click "🔄 Tính toán tự động"
5. Kiểm tra số liệu đã điền
6. Click "Tạo báo cáo"

### Kết Quả Mong Đợi:
- ✅ Báo cáo được tạo thành công
- ✅ Doanh thu chịu thuế khớp với dữ liệu thực tế
- ✅ Số thuế được tính đúng
- ✅ Báo cáo hiển thị trong bảng với trạng thái "Nháp"

### Kết Quả Thực Tế:
- [ ] Pass
- [ ] Fail - Lý do: _______________

---

## Test Case 5: Kiểm Tra Dữ Liệu Nguồn

### Các Bước:
1. Mở SQL client
2. Chạy query:
```sql
SELECT 
    type,
    SUM(amount) as total
FROM financial_transactions
WHERE transaction_date BETWEEN '2025-12-01' AND '2025-12-31'
GROUP BY type;
```

### Kết Quả Mong Đợi:
- ✅ Có dữ liệu REVENUE (doanh thu)
- ✅ Có dữ liệu EXPENSE (chi phí)
- ✅ Số liệu khớp với kết quả tính toán tự động

### Kết Quả Thực Tế:
```
REVENUE: _______________
EXPENSE: _______________
```

---

## Test Case 6: Test API Trực Tiếp

### Sử dụng file: create-tax-from-revenue.http

```http
### 1. Tính toán doanh thu
GET http://localhost:8080/api/accounting/tax/calculate-revenue?periodStart=2025-12-01&periodEnd=2025-12-31
Authorization: Bearer {{token}}

### 2. Tạo báo cáo VAT tự động
POST http://localhost:8080/api/accounting/tax/auto-create?periodStart=2025-12-01&periodEnd=2025-12-31&taxType=VAT
Authorization: Bearer {{token}}
```

### Kết Quả Mong Đợi:
- ✅ API calculate-revenue trả về dữ liệu đúng
- ✅ API auto-create tạo báo cáo thành công
- ✅ Báo cáo có doanh thu chịu thuế từ dữ liệu thực tế

### Kết Quả Thực Tế:
- [ ] Pass
- [ ] Fail - Lý do: _______________

---

## Checklist Tổng Quan

- [ ] Nút "Tính toán tự động" hiển thị trong modal
- [ ] Tính toán VAT hoạt động đúng
- [ ] Tính toán Thuế TNDN hoạt động đúng
- [ ] Validation khi chưa chọn kỳ
- [ ] Tạo báo cáo với dữ liệu tự động thành công
- [ ] Dữ liệu khớp với financial_transactions
- [ ] API endpoints hoạt động đúng

---

## Lưu Ý

### Nếu Không Có Dữ Liệu:
1. Kiểm tra bảng `financial_transactions`
2. Thêm dữ liệu mẫu:
```sql
INSERT INTO financial_transactions (transaction_date, type, category, amount, description, created_by)
VALUES 
('2025-12-15', 'REVENUE', 'SALES', 100000000, 'Doanh thu bán hàng tháng 12', 'ketoan@gmail.com'),
('2025-12-20', 'EXPENSE', 'PURCHASE', 50000000, 'Chi phí nhập hàng', 'ketoan@gmail.com');
```

### Công Thức Tính:
- **VAT (10%)**: Doanh thu × 10%
- **Thuế TNDN (20%)**: (Doanh thu - Chi phí) × 20%

### Troubleshooting:
1. **Lỗi 403**: Kiểm tra token đăng nhập
2. **Không có dữ liệu**: Kiểm tra financial_transactions
3. **Tính toán sai**: Kiểm tra kỳ báo cáo và dữ liệu nguồn

---

## Kết Luận

**Tổng số test cases**: 6
**Số test pass**: ___
**Số test fail**: ___

**Tính năng hoạt động**: [ ] Có [ ] Không

**Ghi chú thêm**:
_______________________________________________
_______________________________________________
_______________________________________________
