# Cleanup Duplicate Fields

## ❌ Đã xóa trùng lặp

### 1. **Order.paidAt** → XÓA

**Lý do:**
- Trùng với `Payment.paidAt`
- Chỉ đơn online mới có Payment
- Đơn COD không cần track paidAt chi tiết

**Cách lấy paidAt:**
```java
// Nếu có Payment
LocalDateTime paidAt = payment.getPaidAt();

// Nếu không có Payment (COD)
// Dùng Order.deliveredAt hoặc không cần
```

## ✅ Giữ lại (Không trùng)

### Order.total vs Payment.amount

**Tại sao giữ cả 2:**
- `Order.total`: Tổng đơn hàng (subtotal + shipping - discount)
- `Payment.amount`: Số tiền thực thanh toán

**Trường hợp khác nhau:**
- Thanh toán một phần
- Hoàn tiền một phần
- Điều chỉnh giá sau khi tạo đơn

**Ví dụ:**
```
Order.total = 100,000 VND
Payment.amount = 50,000 VND (thanh toán một phần)
```

## 📊 Bảng sau khi cleanup

### Order (Business)
```java
- orderCode
- customer, items
- shippingAddress, note
- subtotal, shippingFee, discount, total
- paymentStatus, paymentMethod, paymentId
- status
- createdAt, confirmedAt, shippedAt, deliveredAt
- cancelledAt, cancelReason
```

### Payment (Technical)
```java
- paymentCode
- order, user
- amount, method, status
- sepayTransactionId, sepayBankCode, sepayAccountNumber
- sepayAccountName, sepayContent, sepayQrCode
- sepayResponse
- createdAt, paidAt, expiredAt, failureReason
```

## 🔄 Migration

```sql
-- Xóa column paid_at trong orders
ALTER TABLE orders 
DROP COLUMN IF EXISTS paid_at;
```

## 📝 Code Changes

### 1. Order.java
```java
// ❌ Removed
private LocalDateTime paidAt;

// ✅ Added comment
// Note: paidAt được lấy từ Payment entity (nếu có)
```

### 2. PaymentServiceImpl.java
```java
// ❌ Removed
order.setPaidAt(LocalDateTime.now());

// ✅ Added comment
// Note: paidAt được lưu trong Payment entity
```

### 3. OrderResponse.java (Nếu cần)
```java
// Có thể thêm paidAt từ Payment
private LocalDateTime paidAt; // From Payment entity
```

## 🎯 Lợi ích

1. **Giảm redundancy** - Không lưu trùng dữ liệu
2. **Single source of truth** - paidAt chỉ có trong Payment
3. **Consistency** - Không lo sync 2 field
4. **Cleaner code** - Rõ ràng hơn về responsibility

## 🔍 Cách lấy paidAt

### Backend
```java
// Trong OrderService
LocalDateTime paidAt = null;
if (order.getPaymentId() != null) {
    Payment payment = paymentRepository.findById(order.getPaymentId()).orElse(null);
    if (payment != null) {
        paidAt = payment.getPaidAt();
    }
}
```

### Hoặc dùng JOIN
```java
@Query("SELECT o FROM Order o LEFT JOIN Payment p ON o.paymentId = p.id WHERE o.id = :orderId")
Order findOrderWithPayment(@Param("orderId") Long orderId);
```

### Frontend
```typescript
// Nếu cần hiển thị paidAt
const paidAt = order.paymentId 
  ? await fetchPayment(order.paymentId).then(p => p.paidAt)
  : null;
```

## ⚠️ Breaking Changes

**Nếu code cũ dùng Order.paidAt:**
```java
// ❌ Old code (sẽ lỗi)
order.getPaidAt()

// ✅ New code
Payment payment = paymentRepository.findById(order.getPaymentId()).orElse(null);
LocalDateTime paidAt = payment != null ? payment.getPaidAt() : null;
```

## 🚀 Chạy Migration

```bash
mysql -u root -p web2 < migration_add_pending_payment_status.sql
```

Happy coding! 🎉
