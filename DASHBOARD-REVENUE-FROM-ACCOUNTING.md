# ✅ Dashboard Hiển Thị Doanh Thu Từ Kế Toán

## 🎯 Vấn Đề Đã Fix

**Trước đây**: Dashboard chỉ đếm đơn hàng DELIVERED → Không phản ánh giao dịch thủ công

**Bây giờ**: Dashboard lấy dữ liệu từ `financial_transactions` → Phản ánh TẤT CẢ giao dịch

## 📊 Thay Đổi Logic

### Trước (Chỉ đếm đơn hàng)
```java
Double todayRevenue = orderRepo.findAll().stream()
    .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
    .mapToDouble(Order::getTotal)
    .sum();
```

### Sau (Lấy từ bảng kế toán)
```java
Double todayRevenue = financialTransactionRepo.sumAmountByTypeAndDateRange(
    TransactionType.REVENUE, startOfToday, endOfToday
);
```

## 🔄 Dữ Liệu Hiển Thị

### 1. Tổng Doanh Thu (totalRevenue)
- **Source**: `financial_transactions` với `type = REVENUE`
- **Bao gồm**:
  - Doanh thu từ đơn hàng (tự động khi DELIVERED)
  - Giao dịch thủ công thêm vào (qua trang Giao dịch tài chính)
  - Các khoản thu khác

### 2. Tổng Chi Phí (totalExpense)
- **Source**: `financial_transactions` với `type = EXPENSE`
- **Bao gồm**:
  - Chi phí thanh toán NCC
  - Phí vận chuyển
  - Chi phí khác

### 3. Lợi Nhuận (totalProfit)
- **Công thức**: `totalRevenue - totalExpense`
- **Tỷ suất**: `(totalProfit / totalRevenue) * 100`

### 4. % Thay Đổi
- So sánh với hôm qua
- Tính cho: Revenue, Orders, Profit

## 📝 Cách Hoạt Động

### Khi Thêm Giao Dịch Thủ Công

1. **Vào trang**: `/employee/accounting/transactions`
2. **Thêm giao dịch mới**:
   - Type: REVENUE
   - Category: SALES (hoặc OTHER_REVENUE)
   - Amount: 1,000,000 VND
   - Description: "Bán hàng trực tiếp"

3. **Dashboard tự động cập nhật**:
   - Tổng doanh thu tăng 1,000,000 VND
   - % thay đổi được tính lại
   - Lợi nhuận được cập nhật

### Khi Đơn Hàng DELIVERED

1. **OrderEventListener** tự động tạo `FinancialTransaction`:
   ```java
   FinancialTransaction.builder()
       .type(TransactionType.REVENUE)
       .category(TransactionCategory.SALES)
       .amount(order.getTotal())
       .orderId(order.getId())
       .build();
   ```

2. **Dashboard tự động cập nhật** từ bảng `financial_transactions`

## 🎨 Hiển Thị Trên Frontend

### Admin Dashboard (`/admin`)
```typescript
// Tổng doanh thu hôm nay
{formatPrice(stats.totalRevenue)}

// % thay đổi so với hôm qua
{stats.revenueChangePercent}%

// Lợi nhuận
{formatPrice(stats.totalProfit)}

// Tỷ suất lợi nhuận
{stats.profitMargin.toFixed(1)}%
```

### Accounting Dashboard (`/employee/accounting`)
- Cùng logic, nhưng có thể thêm filter theo kỳ kế toán
- Hiển thị chi tiết hơn (theo category)

## 🔍 Query Được Sử Dụng

### Backend Repository Method
```java
@Query("SELECT SUM(t.amount) FROM FinancialTransaction t " +
       "WHERE t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
Double sumAmountByTypeAndDateRange(
    @Param("type") TransactionType type,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);
```

### SQL Tương Đương
```sql
-- Doanh thu hôm nay
SELECT SUM(amount) 
FROM financial_transactions 
WHERE type = 'REVENUE' 
  AND transaction_date >= '2024-12-27 00:00:00' 
  AND transaction_date <= '2024-12-27 23:59:59';

-- Chi phí hôm nay
SELECT SUM(amount) 
FROM financial_transactions 
WHERE type = 'EXPENSE' 
  AND transaction_date >= '2024-12-27 00:00:00' 
  AND transaction_date <= '2024-12-27 23:59:59';
```

## ✅ Lợi Ích

1. **Chính xác hơn**: Phản ánh tất cả giao dịch, không chỉ đơn hàng
2. **Linh hoạt**: Có thể thêm giao dịch thủ công
3. **Nhất quán**: Dùng chung nguồn dữ liệu với module kế toán
4. **Real-time**: Cập nhật ngay khi có giao dịch mới
5. **Tính lợi nhuận**: Có thể tính profit = revenue - expense

## 🚀 Test

### 1. Test Giao Dịch Thủ Công
```bash
# Thêm giao dịch REVENUE
POST /api/accounting/transactions
{
  "type": "REVENUE",
  "category": "SALES",
  "amount": 1000000,
  "description": "Test manual transaction"
}

# Check dashboard
GET /api/dashboard/stats
# → totalRevenue phải tăng 1,000,000
```

### 2. Test Đơn Hàng Tự Động
```bash
# Tạo đơn hàng và chuyển sang DELIVERED
# → OrderEventListener tự động tạo FinancialTransaction

# Check dashboard
GET /api/dashboard/stats
# → totalRevenue phải tăng theo order.total
```

### 3. Test % Thay Đổi
```bash
# Thêm giao dịch hôm qua (manual trong DB)
INSERT INTO financial_transactions 
(type, category, amount, transaction_date, created_at)
VALUES 
('REVENUE', 'SALES', 500000, '2024-12-26 10:00:00', NOW());

# Check dashboard
GET /api/dashboard/stats
# → revenueChangePercent phải hiển thị % tăng/giảm
```

## 📌 Lưu Ý

1. **Timezone**: Sử dụng `LocalDate.now()` → Theo timezone server
2. **Performance**: Nên thêm index cho `transaction_date` và `type`
3. **Cache**: Có thể cache kết quả 5-10 phút để giảm query
4. **Null check**: Frontend đã có null check cho tất cả fields

---

**Ngày cập nhật**: 2024-12-27  
**Version**: 2.0
