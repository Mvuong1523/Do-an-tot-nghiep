# 🧪 Test Tích Hợp Payment - Accounting

## ✅ Các Thành Phần Đã Tích Hợp

### 1. Event Flow
```
Order Created (PENDING_PAYMENT) 
    ↓
Payment Success (Webhook)
    ↓
Order Status: PENDING_PAYMENT → CONFIRMED
    ↓
OrderStatusChangedEvent Published
    ↓
OrderEventListener.handleOrderStatusChanged()
    ↓
FinancialTransactionService.createTransactionFromOrder()
    ↓
Tạo các giao dịch:
- REVENUE (Doanh thu bán hàng)
- EXPENSE (Phí vận chuyển)  
- EXPENSE (Phí thanh toán 2%)
```

### 2. Database Tables Liên Quan
- `orders` - Đơn hàng
- `payments` - Thanh toán
- `financial_transaction` - Giao dịch tài chính (tự động tạo)
- `payment_reconciliation` - Đối soát thanh toán
- `accounting_period` - Kỳ kế toán

## 🧪 Cách Test

### 1. Test Tạo Đơn Hàng và Thanh Toán

```bash
# 1. Tạo đơn hàng
POST /api/orders
{
  "shippingAddress": "123 Test Street",
  "note": "Test order for accounting"
}

# Response sẽ có orderCode, ví dụ: ORD20241211001

# 2. Tạo payment
POST /api/payment/create
{
  "orderCode": "ORD20241211001"
}

# Response sẽ có paymentCode và QR code

# 3. Simulate webhook (thanh toán thành công)
POST /api/payment/sepay/webhook
{
  "transactionId": "TXN123456789",
  "orderCode": "ORD20241211001", 
  "amount": 1500000,
  "status": "SUCCESS",
  "bankCode": "VCB",
  "content": "Thanh toan don hang ORD20241211001"
}
```

### 2. Kiểm Tra Kết Quả

```bash
# 1. Kiểm tra order status đã chuyển thành CONFIRMED
GET /api/orders/ORD20241211001

# 2. Kiểm tra financial transactions đã được tạo
GET /api/accounting/transactions

# Expected: 3 transactions
# - REVENUE/SALES: 1,500,000 (doanh thu)
# - EXPENSE/SHIPPING: 50,000 (phí VC)
# - EXPENSE/PAYMENT_FEE: 30,000 (phí TT 2%)

# 3. Kiểm tra accounting stats
GET /api/accounting/stats

# Expected: totalRevenue tăng lên
```

### 3. Test Hoàn Tiền

```bash
# 1. Hủy đơn hàng đã thanh toán
PUT /api/admin/orders/1/cancel
{
  "reason": "Customer request refund"
}

# 2. Kiểm tra refund transaction
GET /api/accounting/transactions

# Expected: Thêm 1 transaction REFUND/SALES
```

## 🔧 Troubleshooting

### 1. Event Không Được Publish
```bash
# Check logs cho:
# "Published OrderStatusChangedEvent for order: ORD..."
# "Processing order status change: ORD... -> CONFIRMED"
# "Created financial transactions for order: ORD..."
```

### 2. Financial Transactions Không Tạo
```bash
# Check:
# - OrderEventListener có được load không
# - FinancialTransactionService có hoạt động không
# - Database connection OK không
```

### 3. Accounting Stats Không Cập Nhật
```bash
# Check:
# - Financial transactions có được tạo không
# - Date range trong query có đúng không
# - Order status có đúng là PAID/CONFIRMED không
```

## 📊 Expected Results

### Sau khi thanh toán thành công:

1. **Order Table**:
   - `payment_status`: PAID
   - `status`: CONFIRMED
   - `confirmed_at`: timestamp

2. **Payment Table**:
   - `status`: SUCCESS
   - `paid_at`: timestamp
   - `sepay_transaction_id`: filled

3. **Financial_Transaction Table**:
   - 3 records mới:
     - Revenue transaction
     - Shipping expense
     - Payment fee expense

4. **Accounting Dashboard**:
   - Total revenue tăng
   - Transaction count tăng

## 🎯 Integration Status

### ✅ Completed:
- [x] PaymentService publishes OrderStatusChangedEvent
- [x] OrderService publishes events for status changes
- [x] OrderEventListener handles events
- [x] FinancialTransactionService creates transactions
- [x] Accounting APIs work with financial data

### 🔄 Auto-Generated Data:
- [x] Revenue transactions from orders
- [x] Shipping cost expenses
- [x] Payment gateway fees (2%)
- [x] Refund transactions for cancelled orders

### 📈 Accounting Features Ready:
- [x] Real-time financial tracking
- [x] Payment reconciliation data
- [x] Tax calculation (VAT 10%, Corporate 20%)
- [x] Profit/loss reporting
- [x] Period management with discrepancy checking

**Module kế toán đã tích hợp hoàn chỉnh với hệ thống thanh toán!** 🚀