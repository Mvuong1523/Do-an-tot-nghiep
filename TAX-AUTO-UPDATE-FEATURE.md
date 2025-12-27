# Tính Năng Tự Động Cập Nhật Dữ Liệu Thuế

## 🎯 Tính Năng Mới

Báo cáo thuế ở trạng thái **DRAFT** (Nháp) giờ có thể **tự động cập nhật** doanh thu chịu thuế từ giao dịch mới nhất!

---

## ✅ Đã Hoàn Thành

### Backend:
1. **API mới**: `POST /api/accounting/tax/reports/{id}/recalculate`
2. **Service method**: `recalculateTaxReport(Long id)`
3. **Logic**: Tính lại doanh thu từ `financial_transactions` và cập nhật báo cáo

### Frontend:
1. **Nút "🔄 Cập nhật dữ liệu"** trong bảng báo cáo thuế
2. **Chỉ hiển thị** cho báo cáo ở trạng thái DRAFT
3. **Có ở cả 2 trang**: Admin và Employee

---

## 🎨 Giao Diện

### Bảng Báo Cáo Thuế:

```
┌──────────────────────────────────────────────────────────────┐
│ Mã BC  │ Loại  │ Kỳ  │ DT chịu thuế │ ... │ Trạng thái │ Thao tác │
├──────────────────────────────────────────────────────────────┤
│ VAT-01 │ VAT   │ ... │ 10,000,000 ₫ │ ... │ Nháp       │ 🔄 ✏️ ✓  │
│ VAT-02 │ VAT   │ ... │ 20,000,000 ₫ │ ... │ Đã gửi     │ 💵       │
│ VAT-03 │ VAT   │ ... │ 15,000,000 ₫ │ ... │ Đã nộp     │          │
└──────────────────────────────────────────────────────────────┘
```

**Icons:**
- 🔄 (FiRefreshCw) - Cập nhật dữ liệu
- ✏️ (FiEdit) - Sửa
- ✓ (FiCheck) - Nộp báo cáo
- 💵 (FiDollarSign) - Đánh dấu đã thanh toán

---

## 🔄 Cách Hoạt Động

### Khi Click Nút "Cập nhật dữ liệu":

1. **Kiểm tra trạng thái**: Chỉ cho phép cập nhật báo cáo DRAFT
2. **Lấy kỳ báo cáo**: Từ `periodStart` và `periodEnd`
3. **Tính lại doanh thu**:
   - **VAT**: Tổng REVENUE từ `financial_transactions`
   - **Thuế TNDN**: Lợi nhuận (REVENUE - EXPENSE)
4. **Cập nhật báo cáo**:
   - `taxableRevenue` = doanh thu mới
   - `taxAmount` = doanh thu mới × thuế suất
   - `remainingTax` = taxAmount - paidAmount
5. **Lưu và reload**: Cập nhật database và refresh bảng

---

## 📝 Use Cases

### Use Case 1: Thêm Giao Dịch Mới

**Tình huống:**
- Đã tạo báo cáo thuế VAT tháng 12/2025
- Doanh thu ban đầu: 10,000,000 ₫
- Sau đó thêm giao dịch mới: 5,000,000 ₫

**Giải pháp:**
1. Vào trang Quản lý thuế
2. Tìm báo cáo VAT-122025 (trạng thái Nháp)
3. Click nút 🔄 "Cập nhật dữ liệu"
4. Doanh thu tự động cập nhật: 15,000,000 ₫
5. Thuế VAT cập nhật: 1,500,000 ₫

### Use Case 2: Sửa Giao Dịch

**Tình huống:**
- Báo cáo thuế TNDN Q4/2025
- Lợi nhuận ban đầu: 50,000,000 ₫
- Sửa chi phí → Lợi nhuận mới: 60,000,000 ₫

**Giải pháp:**
1. Click 🔄 "Cập nhật dữ liệu"
2. Lợi nhuận tự động cập nhật: 60,000,000 ₫
3. Thuế TNDN cập nhật: 12,000,000 ₫

### Use Case 3: Xóa Giao Dịch Sai

**Tình huống:**
- Nhập nhầm giao dịch 10,000,000 ₫
- Đã tạo báo cáo thuế
- Xóa giao dịch sai

**Giải pháp:**
1. Xóa giao dịch sai
2. Click 🔄 "Cập nhật dữ liệu" ở báo cáo thuế
3. Doanh thu tự động giảm xuống

---

## 🚀 Cách Sử Dụng

### Bước 1: Tạo Báo Cáo Thuế

1. Vào: http://localhost:3000/employee/accounting/tax
2. Click "Tạo báo cáo thuế"
3. Chọn kỳ và loại thuế
4. Click "🔄 Tính toán tự động" (hoặc nhập thủ công)
5. Tạo báo cáo

### Bước 2: Thêm/Sửa Giao Dịch

1. Vào: http://localhost:3000/employee/accounting/transactions
2. Thêm hoặc sửa giao dịch trong kỳ báo cáo
3. Lưu giao dịch

### Bước 3: Cập Nhật Báo Cáo

1. Quay lại trang Quản lý thuế
2. Tìm báo cáo cần cập nhật (trạng thái Nháp)
3. Click nút 🔄 "Cập nhật dữ liệu"
4. Xác nhận cập nhật
5. Kiểm tra số liệu mới

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Chỉ Cập Nhật Được Báo Cáo DRAFT

- ✅ **DRAFT** (Nháp): Có thể cập nhật
- ❌ **SUBMITTED** (Đã gửi): Không thể cập nhật
- ❌ **PAID** (Đã nộp): Không thể cập nhật

**Lý do**: Báo cáo đã nộp không nên thay đổi để đảm bảo tính nhất quán với cơ quan thuế.

### 2. Quy Trình Đúng

```
1. Tạo báo cáo (DRAFT)
   ↓
2. Thêm/sửa giao dịch
   ↓
3. Cập nhật dữ liệu (🔄)
   ↓
4. Kiểm tra số liệu
   ↓
5. Nộp báo cáo (SUBMITTED)
   ↓
6. Đánh dấu đã thanh toán (PAID)
```

### 3. Không Mất Dữ Liệu Đã Nộp

- `paidAmount` (số đã nộp) **không bị thay đổi**
- Chỉ cập nhật: `taxableRevenue`, `taxAmount`, `remainingTax`

---

## 🧪 Test

### Test 1: Cập Nhật Báo Cáo DRAFT

1. Tạo báo cáo VAT tháng 12/2025 (DRAFT)
2. Doanh thu ban đầu: 10,000,000 ₫
3. Thêm giao dịch mới: 5,000,000 ₫
4. Click 🔄 "Cập nhật dữ liệu"
5. **Kết quả**: Doanh thu = 15,000,000 ₫, Thuế = 1,500,000 ₫

### Test 2: Không Thể Cập Nhật Báo Cáo SUBMITTED

1. Nộp báo cáo (SUBMITTED)
2. Click 🔄 "Cập nhật dữ liệu"
3. **Kết quả**: Lỗi "Chỉ có thể cập nhật báo cáo ở trạng thái Nháp"

### Test 3: Cập Nhật Nhiều Lần

1. Tạo báo cáo (DRAFT)
2. Thêm giao dịch → Click 🔄
3. Thêm giao dịch nữa → Click 🔄 lần 2
4. **Kết quả**: Mỗi lần đều cập nhật đúng

---

## 📊 Ví Dụ Thực Tế

### Tháng 12/2025:

**Ban đầu (01/12):**
- Doanh thu: 10,000,000 ₫
- Tạo báo cáo VAT: 1,000,000 ₫

**Giữa tháng (15/12):**
- Thêm giao dịch: 5,000,000 ₫
- Click 🔄 → Doanh thu: 15,000,000 ₫, Thuế: 1,500,000 ₫

**Cuối tháng (31/12):**
- Thêm giao dịch: 10,000,000 ₫
- Click 🔄 → Doanh thu: 25,000,000 ₫, Thuế: 2,500,000 ₫

**Nộp báo cáo (05/01/2026):**
- Kiểm tra lần cuối
- Click ✓ "Nộp báo cáo"
- Không thể cập nhật nữa

---

## 🔧 Technical Details

### API Endpoint:
```
POST /api/accounting/tax/reports/{id}/recalculate
Authorization: Bearer {token}
```

### Request:
```
Không cần body, chỉ cần id trong URL
```

### Response Success:
```json
{
  "success": true,
  "message": "Cập nhật dữ liệu báo cáo thuế thành công",
  "data": {
    "id": 1,
    "reportCode": "VAT-122025",
    "taxableRevenue": 15000000,
    "taxAmount": 1500000,
    "remainingTax": 1500000,
    ...
  }
}
```

### Response Error:
```json
{
  "success": false,
  "message": "Chỉ có thể cập nhật báo cáo ở trạng thái Nháp"
}
```

---

## 📋 Checklist

### Backend:
- [x] Thêm method `recalculateTaxReport` vào Service
- [x] Thêm endpoint `/reports/{id}/recalculate` vào Controller
- [x] Kiểm tra trạng thái DRAFT
- [x] Tính lại doanh thu từ financial_transactions
- [x] Cập nhật taxableRevenue, taxAmount, remainingTax

### Frontend:
- [x] Thêm icon FiRefreshCw
- [x] Thêm function `recalculateTaxReport`
- [x] Thêm nút 🔄 trong bảng (chỉ cho DRAFT)
- [x] Hiển thị confirmation dialog
- [x] Hiển thị toast thông báo
- [x] Reload data sau khi cập nhật
- [x] Có ở cả Admin và Employee

### Test:
- [ ] Restart backend
- [ ] Restart frontend
- [ ] Test cập nhật báo cáo DRAFT
- [ ] Test không thể cập nhật SUBMITTED
- [ ] Test với nhiều giao dịch
- [ ] Test cả VAT và Thuế TNDN

---

## 🎉 Lợi Ích

### Trước đây:
- ❌ Phải xóa báo cáo cũ
- ❌ Tạo lại báo cáo mới
- ❌ Mất thời gian
- ❌ Dễ nhầm lẫn

### Bây giờ:
- ✅ Chỉ cần click 1 nút
- ✅ Tự động cập nhật
- ✅ Nhanh chóng
- ✅ Chính xác

---

## 🚀 Bước Tiếp Theo

1. **Restart backend**: Áp dụng API mới
2. **Restart frontend**: Hiển thị nút mới
3. **Test**: Thử cập nhật báo cáo
4. **Sử dụng**: Tận hưởng tính năng mới!

**Hãy restart cả backend và frontend để sử dụng tính năng này!** 🎊
