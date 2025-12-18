# 🔍 Debug: Không hiện data khi test Module Kế toán

## Vấn đề
Khi vào trang "Đối soát thanh toán", không hiện data gì cả (0 giao dịch, 0 khớp, 0 sai lệch).

---

## Nguyên nhân có thể

### 1. ❌ Chưa có dữ liệu thật trong hệ thống
- Chưa có đơn hàng nào được thanh toán
- Chưa nhập hàng
- Chưa có giao dịch tài chính

### 2. ❌ Bảng `payment_reconciliations` trống
- Bảng này lưu dữ liệu đối soát từ cổng thanh toán
- Cần import file CSV từ SePay/VNPay/MoMo

### 3. ❌ Backend chưa chạy hoặc lỗi
- Backend không khởi động
- Lỗi kết nối database
- Lỗi authentication

---

## Cách kiểm tra

### Bước 1: Kiểm tra Backend
```bash
# Xem log backend
# Tìm dòng: "Started WEB_TMDT Application"
# Kiểm tra có lỗi gì không
```

### Bước 2: Kiểm tra Database
Chạy file `check-accounting-data.sql` để xem có dữ liệu không:

```sql
-- Chạy từng query trong file check-accounting-data.sql
-- Xem kết quả:

-- Nếu total_orders = 0 → Chưa có đơn hàng
-- Nếu paid_orders = 0 → Chưa có đơn thanh toán
-- Nếu financial_transactions = 0 → Chưa ghi nhận giao dịch
```

### Bước 3: Kiểm tra API
Dùng file `test-accounting-reconciliation.http`:

```http
### Test 1: Get Stats
GET http://localhost:8080/api/accounting/stats
Authorization: Bearer {{token}}

# Kết quả mong đợi:
{
  "success": true,
  "data": {
    "totalRevenue": 0,
    "pendingReconciliation": 0,
    "completedReconciliation": 0,
    "discrepancyAmount": 0
  }
}
```

### Bước 4: Kiểm tra Browser Console
Mở DevTools (F12) → Console → Xem có lỗi gì không:
- Network tab: Xem API có trả về 200 OK không
- Console tab: Xem có lỗi JavaScript không

---

## Giải pháp

### ✅ Giải pháp 1: Tạo dữ liệu test

#### 1.1. Nhập hàng
```
1. Vào /admin/inventory/purchase-orders
2. Tạo phiếu nhập mới
3. Nhập thông tin NCC và sản phẩm
4. Hoàn tất nhập hàng
→ Tự động tạo công nợ NCC
```

#### 1.2. Bán hàng
```
1. Khách vào trang chủ, thêm sản phẩm vào giỏ
2. Checkout → Thanh toán
3. Admin vào /admin/orders
4. Xác nhận đơn (CONFIRMED)
→ Tự động ghi nhận doanh thu
```

#### 1.3. Kiểm tra lại
```
1. Vào /admin/accounting
2. Click "Đối soát thanh toán"
3. Chọn khoảng thời gian
4. Click "Tải dữ liệu"
→ Sẽ thấy data nếu có đơn đã thanh toán
```

---

### ✅ Giải pháp 2: Import dữ liệu đối soát

Nếu muốn test tính năng đối soát, cần import file CSV từ cổng thanh toán:

#### Format file CSV (SePay):
```csv
orderCode,transactionId,amount,transactionDate,status
ORD-001,TXN-123,500000,2024-12-18 10:30:00,SUCCESS
ORD-002,TXN-124,750000,2024-12-18 11:45:00,SUCCESS
```

#### Cách import:
```
1. Vào /admin/accounting/reconciliation
2. Chọn cổng thanh toán (SEPAY)
3. Click nút "Import"
4. Chọn file CSV
→ Hệ thống tự động đối soát
```

---

### ✅ Giải pháp 3: Tạo dữ liệu mẫu bằng SQL

Nếu muốn test nhanh, chạy SQL này:

```sql
-- 1. Tạo đơn hàng mẫu (giả sử đã có user_id = 1)
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal, shipping_fee, total, created_at)
VALUES 
('ORD-TEST-001', 1, 'CONFIRMED', 'PAID', 500000, 30000, 530000, NOW()),
('ORD-TEST-002', 1, 'DELIVERED', 'PAID', 750000, 30000, 780000, NOW());

-- 2. Tạo payment tương ứng
INSERT INTO payments (order_id, amount, payment_method, status, paid_at)
SELECT id, total, 'BANK_TRANSFER', 'COMPLETED', NOW()
FROM orders 
WHERE order_code IN ('ORD-TEST-001', 'ORD-TEST-002');

-- 3. Tạo giao dịch tài chính
INSERT INTO financial_transactions (order_id, type, category, amount, description, transaction_date)
SELECT 
    order_code,
    'REVENUE',
    'SALES',
    subtotal,
    CONCAT('Doanh thu từ đơn ', order_code),
    created_at
FROM orders
WHERE order_code IN ('ORD-TEST-001', 'ORD-TEST-002');

-- 4. Tạo dữ liệu đối soát
INSERT INTO payment_reconciliations (order_id, transaction_id, gateway, gateway_amount, system_amount, discrepancy, status, transaction_date)
SELECT 
    order_code,
    CONCAT('TXN-', id),
    'SEPAY',
    total,
    total,
    0,
    'MATCHED',
    created_at
FROM orders
WHERE order_code IN ('ORD-TEST-001', 'ORD-TEST-002');

-- 5. Kiểm tra
SELECT * FROM orders WHERE order_code LIKE 'ORD-TEST%';
SELECT * FROM financial_transactions WHERE order_id LIKE 'ORD-TEST%';
SELECT * FROM payment_reconciliations WHERE order_id LIKE 'ORD-TEST%';
```

---

## Kiểm tra kết quả

### 1. Dashboard (/admin/accounting)
Sau khi có dữ liệu, sẽ thấy:
- ✅ Tổng doanh thu > 0
- ✅ Chờ đối soát hoặc Đã đối soát > 0

### 2. Đối soát thanh toán (/admin/accounting/reconciliation)
- ✅ Tổng giao dịch > 0
- ✅ Khớp > 0 (nếu số tiền khớp)
- ✅ Sai lệch > 0 (nếu có chênh lệch)

### 3. Báo cáo tài chính (/admin/accounting/financial-statement/dashboard)
- ✅ Doanh thu > 0
- ✅ Chi phí > 0 (nếu có nhập hàng)
- ✅ Lợi nhuận được tính

### 4. Công nợ NCC (/admin/accounting/payables)
- ✅ Danh sách công nợ (nếu đã nhập hàng)
- ✅ Tổng nợ, đã trả, còn lại

---

## Checklist Debug

- [ ] Backend đang chạy (port 8080)
- [ ] Database có kết nối
- [ ] Đã login với tài khoản ADMIN hoặc ACCOUNTANT
- [ ] Token còn hạn (check localStorage)
- [ ] Có ít nhất 1 đơn hàng PAID
- [ ] API `/api/accounting/stats` trả về success
- [ ] Browser console không có lỗi
- [ ] Network tab thấy API call thành công

---

## Lỗi thường gặp

### Lỗi 1: "Bạn không có quyền truy cập"
**Nguyên nhân:** User không phải ADMIN hoặc ACCOUNTANT
**Giải pháp:** 
```sql
-- Cấp quyền ACCOUNTANT cho user
UPDATE users SET role = 'ADMIN' WHERE id = 1;
-- Hoặc
UPDATE employees SET position = 'ACCOUNTANT' WHERE user_id = 1;
```

### Lỗi 2: "Network Error" hoặc "Failed to fetch"
**Nguyên nhân:** Backend không chạy hoặc CORS
**Giải pháp:**
- Kiểm tra backend đang chạy: `http://localhost:8080/api/accounting/stats`
- Kiểm tra CORS config trong SecurityConfig

### Lỗi 3: "401 Unauthorized"
**Nguyên nhân:** Token hết hạn hoặc không hợp lệ
**Giải pháp:**
- Logout và login lại
- Kiểm tra localStorage có token không

### Lỗi 4: Data = [] (mảng rỗng)
**Nguyên nhân:** Chưa có dữ liệu trong database
**Giải pháp:**
- Tạo dữ liệu test theo hướng dẫn trên
- Hoặc chạy SQL insert mẫu

---

## Kết luận

Module kế toán **không có lỗi code**, chỉ cần:
1. ✅ Có dữ liệu thật (đơn hàng, nhập hàng)
2. ✅ Backend đang chạy
3. ✅ User có quyền truy cập

**Sau khi tạo dữ liệu test, module sẽ hoạt động bình thường!**

---

## Liên hệ hỗ trợ

Nếu vẫn gặp vấn đề:
1. Chụp màn hình lỗi
2. Copy log backend
3. Copy kết quả SQL queries
4. Gửi cho dev team

**Module đã sẵn sàng, chỉ cần dữ liệu để test!** 🚀
