# ✅ Cập nhật: Hiển thị tất cả đơn hàng trong Đối soát thanh toán

## Thay đổi

### Trước đây
- Trang "Đối soát thanh toán" chỉ hiển thị các giao dịch đã import từ cổng thanh toán (bảng `payment_reconciliations`)
- Không thấy đơn hàng nào nếu chưa import file CSV từ SePay/VNPay/MoMo
- Chỉ thấy đơn đã thanh toán và đã xác nhận

### Bây giờ
- ✅ Hiển thị **TẤT CẢ đơn hàng** trong khoảng thời gian được chọn
- ✅ Bao gồm cả đơn **chưa thanh toán** (PENDING_PAYMENT)
- ✅ Bao gồm cả đơn **chưa xác nhận** (PENDING, CANCELLED, v.v.)
- ✅ Hiển thị trạng thái đơn hàng và trạng thái thanh toán
- ✅ Tự động đối soát với dữ liệu từ gateway (nếu có)

---

## Chi tiết thay đổi

### 1. Backend - AccountingServiceImpl.java

#### Method: `getPaymentReconciliation()`

**Trước:**
```java
// Chỉ lấy từ bảng payment_reconciliations
List<PaymentReconciliation> reconciliations = 
    reconciliationRepo.findByTransactionDateBetween(startDateTime, endDateTime);
```

**Sau:**
```java
// Lấy TẤT CẢ đơn hàng từ bảng orders
List<Order> allOrders = orderRepo.findByCreatedAtBetween(startDateTime, endDateTime);

// Lấy dữ liệu đối soát (nếu có)
List<PaymentReconciliation> existingReconciliations = 
    reconciliationRepo.findByTransactionDateBetween(startDateTime, endDateTime);

// Kết hợp 2 nguồn dữ liệu
for (Order order : allOrders) {
    // Tạo item cho mỗi đơn hàng
    // Nếu có dữ liệu gateway → hiển thị đối soát
    // Nếu không → hiển thị trạng thái đơn hàng
}
```

#### Trạng thái mới

| Trạng thái | Ý nghĩa | Màu sắc |
|-----------|---------|---------|
| `MATCHED` | Số tiền khớp giữa hệ thống và gateway | Xanh lá |
| `MISMATCHED` | Số tiền sai lệch | Đỏ |
| `MISSING_IN_GATEWAY` | Đã thanh toán nhưng chưa có trong gateway | Cam |
| `MISSING_IN_SYSTEM` | Có trong gateway nhưng không có trong hệ thống | Cam |
| `PENDING_PAYMENT` | Chưa thanh toán | Vàng |

---

### 2. Frontend - reconciliation/page.tsx

#### Thêm cột mới

**Trước:** 7 cột
- Mã đơn
- Mã GD
- Cổng TT
- Hệ thống
- Cổng TT
- Sai lệch
- Trạng thái

**Sau:** 9 cột
- Mã đơn
- **TT Đơn** (mới)
- **TT Thanh toán** (mới)
- Mã GD
- Cổng TT
- Hệ thống
- Cổng TT
- Sai lệch
- Trạng thái

#### Summary mới

**Trước:** 4 thẻ
- Tổng giao dịch
- Khớp
- Sai lệch
- Thiếu

**Sau:** 5 thẻ
- Tổng đơn hàng
- Khớp
- Sai lệch
- Thiếu gateway
- **Chưa thanh toán** (mới)

#### Highlight đơn chưa thanh toán
```tsx
<tr className={item.paymentStatus === 'PENDING' ? 'bg-gray-50' : ''}>
```
→ Đơn chưa thanh toán có nền xám nhạt

---

## Kết quả

### Trước khi sửa
```
Trang trống, không có data
→ Vì chưa import file từ gateway
```

### Sau khi sửa
```
Hiển thị TẤT CẢ đơn hàng:

Mã đơn         | TT Đơn      | TT Thanh toán | Trạng thái
ORD-001        | Đã xác nhận | Đã thanh toán | Khớp
ORD-002        | Đang xử lý  | Đã thanh toán | Thiếu gateway
ORD-003        | Chờ TT      | Chờ thanh toán| Chưa TT
ORD-004        | Đã hủy      | Chờ thanh toán| Chưa TT
```

---

## Lợi ích

### 1. Theo dõi toàn diện
- ✅ Thấy được tất cả đơn hàng, không bỏ sót
- ✅ Biết được đơn nào chưa thanh toán
- ✅ Biết được đơn nào đã thanh toán nhưng chưa có trong gateway

### 2. Phát hiện vấn đề sớm
- ✅ Đơn đã thanh toán nhưng thiếu trong gateway → Cần kiểm tra
- ✅ Đơn chưa thanh toán lâu → Cần nhắc khách
- ✅ Đơn bị hủy sau khi thanh toán → Cần hoàn tiền

### 3. Không cần import file
- ✅ Có thể xem ngay mà không cần import CSV
- ✅ Import file chỉ để đối soát chi tiết
- ✅ Vẫn thấy được tổng quan đơn hàng

---

## Cách sử dụng

### Bước 1: Vào trang Đối soát
```
/admin/accounting/reconciliation
```

### Bước 2: Chọn khoảng thời gian
```
Từ ngày: 11/18/2024
Đến ngày: 12/18/2024
Cổng thanh toán: Tất cả
```

### Bước 3: Click "Tải dữ liệu"
```
→ Hiển thị TẤT CẢ đơn hàng trong khoảng thời gian
```

### Bước 4: Phân tích
```
- Xem tổng đơn hàng
- Xem bao nhiêu đơn đã thanh toán
- Xem bao nhiêu đơn chưa thanh toán
- Xem đơn nào sai lệch
- Xem đơn nào thiếu trong gateway
```

### Bước 5: Import file (tùy chọn)
```
- Nếu muốn đối soát chi tiết
- Click "Import"
- Chọn file CSV từ SePay/VNPay/MoMo
→ Hệ thống tự động đối soát
```

---

## Ví dụ thực tế

### Trường hợp 1: Đơn đã thanh toán, có trong gateway
```
Mã đơn: ORD-001
TT Đơn: Đã xác nhận
TT Thanh toán: Đã thanh toán
Mã GD: SEPAY-TXN-001
Cổng TT: SEPAY
Hệ thống: 1,530,000 ₫
Cổng TT: 1,530,000 ₫
Sai lệch: -
Trạng thái: Khớp ✅
```

### Trường hợp 2: Đơn đã thanh toán, chưa có trong gateway
```
Mã đơn: ORD-002
TT Đơn: Đang xử lý
TT Thanh toán: Đã thanh toán
Mã GD: -
Cổng TT: -
Hệ thống: 2,530,000 ₫
Cổng TT: -
Sai lệch: -
Trạng thái: Thiếu gateway ⚠️
→ Cần kiểm tra: Tại sao đã thanh toán mà chưa có trong gateway?
```

### Trường hợp 3: Đơn chưa thanh toán
```
Mã đơn: ORD-003
TT Đơn: Chờ TT
TT Thanh toán: Chờ thanh toán
Mã GD: -
Cổng TT: -
Hệ thống: 3,230,000 ₫
Cổng TT: -
Sai lệch: -
Trạng thái: Chưa TT 🔵
→ Bình thường, chờ khách thanh toán
```

### Trường hợp 4: Sai lệch số tiền
```
Mã đơn: ORD-004
TT Đơn: Đã xác nhận
TT Thanh toán: Đã thanh toán
Mã GD: SEPAY-TXN-004
Cổng TT: SEPAY
Hệ thống: 1,830,000 ₫
Cổng TT: 1,840,000 ₫
Sai lệch: 10,000 ₫
Trạng thái: Sai lệch ❌
→ Cần kiểm tra: Tại sao gateway ghi nhận nhiều hơn?
```

---

## Testing

### Test 1: Không có đơn hàng nào
```
Kết quả: 
- Tổng đơn hàng: 0
- Bảng trống
- Không có lỗi
```

### Test 2: Có đơn chưa thanh toán
```
Kết quả:
- Hiển thị đơn với trạng thái "Chưa TT"
- Nền xám nhạt
- Không có mã GD, không có cổng TT
```

### Test 3: Có đơn đã thanh toán, chưa import
```
Kết quả:
- Hiển thị đơn với trạng thái "Thiếu gateway"
- Có số tiền hệ thống
- Không có số tiền gateway
```

### Test 4: Có đơn đã thanh toán, đã import
```
Kết quả:
- Hiển thị đầy đủ thông tin
- Có mã GD, có cổng TT
- Trạng thái "Khớp" hoặc "Sai lệch"
```

---

## Files đã sửa

1. ✅ `src/main/java/com/doan/WEB_TMDT/module/accounting/service/impl/AccountingServiceImpl.java`
   - Method `getPaymentReconciliation()` - Lấy tất cả đơn hàng
   - Method `calculateSummaryFromResults()` - Tính summary mới

2. ✅ `src/frontend/app/admin/accounting/reconciliation/page.tsx`
   - Thêm 2 cột: TT Đơn, TT Thanh toán
   - Thêm summary: Chưa thanh toán
   - Highlight đơn chưa thanh toán
   - Hiển thị trạng thái chi tiết hơn

---

## Kết luận

✅ **Đã hoàn thành!**

Trang "Đối soát thanh toán" bây giờ hiển thị **TẤT CẢ đơn hàng**, kể cả:
- Đơn chưa thanh toán
- Đơn chưa xác nhận
- Đơn đã hủy
- Đơn đang xử lý

→ Giúp theo dõi toàn diện và phát hiện vấn đề sớm!

**Không còn tình trạng "không có data" nữa!** 🎉
