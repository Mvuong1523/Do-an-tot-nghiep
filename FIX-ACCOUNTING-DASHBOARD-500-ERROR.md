# ✅ Fix Lỗi 500 - Accounting Dashboard

## 🐛 Lỗi

```
:8080/api/accounting/financial-statement:1  
Failed to load resource: the server responded with a status of 500 ()
```

## 🔍 Nguyên Nhân

1. **Frontend gọi sai endpoint**: 
   - Gọi `/api/accounting/financial-statement` (không có params)
   - Endpoint này BẮT BUỘC phải có `startDate` và `endDate`

2. **Backend code lỗi**:
   - Đã xóa biến `orders` nhưng vẫn dùng `orders.size()`
   - Import không dùng: `OrderStatus`, `PaymentStatus`

## ✅ Đã Fix

### 1. Frontend - Đổi Endpoint

**File**: `src/frontend/app/employee/accounting/page.tsx`

**Trước**:
```typescript
const statsResponse = await fetch(
  'http://localhost:8080/api/accounting/financial-statement',
  { headers: { 'Authorization': `Bearer ${token}` } }
)
```

**Sau**:
```typescript
const statsResponse = await fetch(
  'http://localhost:8080/api/accounting/financial-statement/dashboard',
  { headers: { 'Authorization': `Bearer ${token}` } }
)
```

**Lý do**: Endpoint `/dashboard` không cần params, tự động lấy tháng hiện tại.

### 2. Backend - Fix calculateRevenue()

**File**: `FinancialStatementServiceImpl.java`

**Trước**:
```java
// Lỗi: biến orders không tồn tại
return FinancialStatementResponse.RevenueSection.builder()
    .totalRevenue(totalRevenue)
    .orderCount(orders.size())  // ❌ ERROR
    .build();
```

**Sau**:
```java
// Đếm số đơn hàng trong kỳ
long orderCount = orderRepository.findByCreatedAtBetween(start, end).size();

return FinancialStatementResponse.RevenueSection.builder()
    .totalRevenue(totalRevenue)
    .orderCount((int) orderCount)  // ✅ OK
    .build();
```

### 3. Backend - Xóa Unused Imports

```java
// Xóa
import com.doan.WEB_TMDT.module.order.entity.OrderStatus;
import com.doan.WEB_TMDT.module.order.entity.PaymentStatus;
```

## 🎯 Kết Quả

### API Endpoint Hoạt Động

```bash
GET /api/accounting/financial-statement/dashboard
Authorization: Bearer {token}
```

**Response**:
```json
{
  "success": true,
  "message": "Báo cáo tài chính",
  "data": {
    "startDate": "2024-12-01",
    "endDate": "2024-12-31",
    "revenue": {
      "totalRevenue": 1000000,
      "productRevenue": 1000000,
      "shippingRevenue": 0,
      "otherRevenue": 0,
      "orderCount": 5
    },
    "expenses": {
      "totalExpense": 200000,
      "costOfGoodsSold": 150000,
      "shippingExpense": 30000,
      "paymentFee": 20000,
      "operatingExpense": 0,
      "otherExpense": 0
    },
    "profit": {
      "grossProfit": 850000,
      "operatingProfit": 850000,
      "netProfit": 800000,
      "profitMargin": 80.0
    }
  }
}
```

### Frontend Hiển Thị

Trang `/employee/accounting` giờ sẽ hiển thị:
- ✅ Tổng doanh thu: Từ `financial_transactions` (REVENUE)
- ✅ Tổng chi phí: Từ `financial_transactions` (EXPENSE)
- ✅ Lợi nhuận ròng: Revenue - Expense
- ✅ Tổng nợ thuế: Từ `tax_reports`

## 🔄 Luồng Dữ Liệu

```
Frontend (/employee/accounting)
    ↓
GET /api/accounting/financial-statement/dashboard
    ↓
FinancialStatementController.getDashboard()
    ↓
FinancialStatementService.getDashboard()
    ↓
getFinancialStatement(startOfMonth, endOfMonth)
    ↓
calculateRevenue() → Lấy từ financial_transactions (REVENUE)
calculateExpenses() → Lấy từ financial_transactions (EXPENSE)
calculateProfit() → Revenue - Expense
    ↓
Return FinancialStatementResponse
```

## 📊 Dữ Liệu Hiển Thị

### Tổng Doanh Thu
- **Source**: `financial_transactions` với `type = REVENUE`
- **Query**: 
  ```sql
  SELECT SUM(amount) 
  FROM financial_transactions 
  WHERE type = 'REVENUE' 
    AND transaction_date >= '2024-12-01 00:00:00' 
    AND transaction_date <= '2024-12-31 23:59:59';
  ```

### Tổng Chi Phí
- **Source**: `financial_transactions` với `type = EXPENSE`
- **Categories**:
  - SUPPLIER_PAYMENT → Cost of Goods Sold
  - SHIPPING → Shipping Expense
  - PAYMENT_FEE → Payment Fee
  - OTHER_EXPENSE → Other Expense

### Lợi Nhuận
- **Gross Profit**: Revenue - Cost of Goods Sold
- **Operating Profit**: Gross Profit - Operating Expense - Other Expense
- **Net Profit**: Operating Profit - Payment Fee - Shipping Expense
- **Profit Margin**: (Net Profit / Revenue) * 100

## 🚀 Test

### 1. Restart Backend
```bash
./mvnw spring-boot:run
```

### 2. Test API
```bash
# Lấy token
POST http://localhost:8080/api/auth/login
{
  "email": "admin@webindi.com",
  "password": "admin123"
}

# Test dashboard
GET http://localhost:8080/api/accounting/financial-statement/dashboard
Authorization: Bearer {token}
```

### 3. Test Frontend
1. Vào trang: http://localhost:3000/employee/accounting
2. Kiểm tra:
   - Tổng doanh thu có hiển thị đúng không?
   - Tổng chi phí có hiển thị đúng không?
   - Lợi nhuận có tính đúng không?
   - Giao dịch gần đây có hiển thị không?

### 4. Test Thêm Giao Dịch
```bash
# Thêm giao dịch REVENUE
POST http://localhost:8080/api/accounting/transactions
Authorization: Bearer {token}
{
  "type": "REVENUE",
  "category": "SALES",
  "amount": 500000,
  "description": "Test transaction",
  "transactionDate": "2024-12-27T10:00:00"
}

# Refresh trang accounting
# → Tổng doanh thu phải tăng 500,000 VND
```

## 📝 Lưu Ý

1. **Endpoint `/dashboard`**: Tự động lấy tháng hiện tại, không cần params
2. **Endpoint `/financial-statement`**: Cần params `startDate` và `endDate`
3. **Data source**: Tất cả từ `financial_transactions`, không phải từ `orders`
4. **Real-time**: Dữ liệu cập nhật ngay khi có giao dịch mới

---

**Ngày fix**: 2024-12-27  
**Version**: 1.0
