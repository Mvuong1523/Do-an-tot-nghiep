# Module Kế toán - Hướng dẫn Clean Code

## 🎯 Nguyên tắc: CHỈ LÀM VIỆC VỚI DATA THẬT

Không có giả định, không có dữ liệu mẫu phức tạp. Tất cả từ đơn hàng thực tế.

## 📊 Dữ liệu tự động

### Khi đơn hàng được thanh toán → Tạo 3 giao dịch:

```java
// 1. Doanh thu
SALES = Order.total

// 2. Chi phí vận chuyển  
SHIPPING = Order.shippingFee

// 3. Phí thanh toán
PAYMENT_FEE = Order.total × 2%
```

### Khi đơn hàng bị hủy (đã thanh toán) → Tạo 1 giao dịch:

```java
REFUND = Order.total
```

## 💰 Công thức tính lợi nhuận

```
Doanh thu                    = 1,000,000 ₫
- Chi phí vận chuyển         =    30,000 ₫
- Phí thanh toán (2%)        =    20,000 ₫
─────────────────────────────────────────
= Lợi nhuận gộp              =   950,000 ₫

- VAT (10%)                  =    95,000 ₫
─────────────────────────────────────────
= Lợi nhuận sau VAT          =   855,000 ₫

- Thuế TNDN (20%)            =   171,000 ₫
─────────────────────────────────────────
= Lợi nhuận ròng             =   684,000 ₫
```

## 📁 Cấu trúc code

### Backend - 4 files chính:

1. **TransactionCategory.java** - 4 enum values
   ```java
   SALES, SHIPPING, PAYMENT_FEE, TAX
   ```

2. **FinancialTransactionServiceImpl.java** - Logic tạo giao dịch
   - `createTransactionFromOrder()` - Tạo 3 giao dịch khi thanh toán
   - `createRefundTransaction()` - Tạo giao dịch hoàn tiền

3. **AccountingServiceImpl.java** - Logic tính toán báo cáo
   - `calculateOrderFinancials()` - Tính cho 1 đơn
   - `calculatePeriodFinancials()` - Tính cho nhiều đơn

4. **OrderEventListener.java** - Lắng nghe sự kiện đơn hàng
   - Khi PAID → Tạo giao dịch
   - Khi CANCELLED → Tạo hoàn tiền

### Frontend - 3 pages:

1. **transactions/page.tsx** - Danh sách giao dịch
2. **reports/page.tsx** - Báo cáo tài chính (10 cột)
3. **advanced-reports/page.tsx** - Báo cáo lãi lỗ chi tiết

## 🚀 Cách test

### Bước 1: Tạo đơn hàng
```
Subtotal: 1,000,000 ₫
Shipping: 30,000 ₫
Total: 1,030,000 ₫
```

### Bước 2: Thanh toán đơn hàng
- Chuyển trạng thái → PAID

### Bước 3: Kiểm tra giao dịch
Vào `/admin/accounting/transactions` sẽ thấy:

| Loại | Danh mục | Số tiền |
|------|----------|---------|
| Thu | Doanh thu bán hàng | +1,030,000 ₫ |
| Chi | Chi phí vận chuyển | -30,000 ₫ |
| Chi | Phí cổng thanh toán | -20,600 ₫ |

### Bước 4: Xem báo cáo
Vào `/admin/accounting/reports`:
- Doanh thu: 1,030,000 ₫
- Tổng chi phí: 50,600 ₫
- Lợi nhuận gộp: 979,400 ₫
- VAT: 97,940 ₫
- Thuế TNDN: 176,292 ₫
- Lợi nhuận ròng: 705,168 ₫

## ✅ Lợi ích

- **Dễ đọc**: Không có logic phức tạp
- **Dễ hiểu**: Công thức rõ ràng
- **Dễ fix**: Ít code, ít bug
- **Dễ test**: Tạo đơn → Xem kết quả
- **Data thật**: Không có giả định

## 🔧 Khi cần thêm tính năng

### Ví dụ: Thêm chi phí bảo hành

1. Thêm vào enum:
```java
public enum TransactionCategory {
    SALES, SHIPPING, PAYMENT_FEE, TAX,
    WARRANTY  // ← Thêm mới
}
```

2. Tạo giao dịch thủ công:
```java
POST /api/accounting/transactions
{
  "type": "EXPENSE",
  "category": "WARRANTY",
  "amount": 100000,
  "description": "Bảo hành sản phẩm X"
}
```

3. Cập nhật công thức tính (nếu cần):
```java
BigDecimal warrantyCosts = getAmountByCategory(
    TransactionCategory.WARRANTY, startDate, endDate
);
totalExpenses = totalExpenses.add(warrantyCosts);
```

## 📝 Ghi chú

- Không tạo dữ liệu mẫu khi khởi động
- Tất cả giao dịch từ đơn hàng thật
- Chốt kỳ tính doanh thu từ đơn hàng trong kỳ
- Sai số >15% → Không cho chốt kỳ
