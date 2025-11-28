# Thêm Payment Method Field

## ✅ Đã cập nhật

### 1. Backend

**Order.java Entity:**
```java
@Column(length = 20)
private String paymentMethod; // COD, SEPAY, VNPAY, etc.
```

**OrderResponse.java DTO:**
```java
private String paymentMethod; // COD, SEPAY, VNPAY, etc.
```

**OrderServiceImpl.java:**
```java
// Khi tạo đơn
Order order = Order.builder()
    // ...
    .paymentMethod(request.getPaymentMethod())
    .build();

// Khi trả về response
OrderResponse.builder()
    // ...
    .paymentMethod(order.getPaymentMethod())
    .build();
```

### 2. Database Migration

**File:** `migration_add_pending_payment_status.sql`

```sql
-- Thêm column payment_method
ALTER TABLE orders 
ADD COLUMN payment_method VARCHAR(20) NULL AFTER payment_status;

-- Cập nhật dữ liệu cũ
UPDATE orders 
SET payment_method = CASE 
    WHEN payment_id IS NULL THEN 'COD'
    ELSE 'SEPAY'
END
WHERE payment_method IS NULL;
```

## 🎯 Giá trị có thể

| Value | Mô tả |
|-------|-------|
| `COD` | Thanh toán khi nhận hàng |
| `SEPAY` | Thanh toán qua SePay (QR Code) |
| `VNPAY` | Thanh toán qua VNPay (future) |
| `MOMO` | Thanh toán qua MoMo (future) |
| `ZALOPAY` | Thanh toán qua ZaloPay (future) |

## 📊 Sử dụng

### Backend
```java
// Lấy payment method từ order
String method = order.getPaymentMethod();

// Check payment method
if ("COD".equals(order.getPaymentMethod())) {
    // COD logic
} else if ("SEPAY".equals(order.getPaymentMethod())) {
    // Online payment logic
}
```

### Frontend
```typescript
// Hiển thị payment method
const paymentMethodText = {
  'COD': 'Thanh toán khi nhận hàng',
  'SEPAY': 'Thanh toán online (QR Code)',
  'VNPAY': 'Thanh toán qua VNPay',
  'MOMO': 'Thanh toán qua MoMo'
}[order.paymentMethod] || order.paymentMethod;
```

## 🔄 Flow

### Tạo đơn hàng
```
1. Frontend gửi request với paymentMethod
   {
     "paymentMethod": "SEPAY",
     // ...
   }
   ↓
2. Backend lưu vào Order entity
   order.setPaymentMethod("SEPAY")
   ↓
3. Database lưu vào column payment_method
```

### Hiển thị đơn hàng
```
1. Backend query Order
   ↓
2. Map sang OrderResponse
   response.setPaymentMethod(order.getPaymentMethod())
   ↓
3. Frontend nhận và hiển thị
   "Thanh toán online (QR Code)"
```

## ✅ Lợi ích

1. **Phân biệt rõ ràng** - Biết đơn thanh toán bằng gì
2. **Báo cáo tốt hơn** - Thống kê theo payment method
3. **Logic rõ ràng** - Xử lý khác nhau cho từng method
4. **Mở rộng dễ** - Thêm payment method mới dễ dàng
5. **Debug dễ** - Biết đơn dùng payment nào

## 🚀 Chạy Migration

```bash
# Cách 1: MySQL Command Line
mysql -u root -p web2 < migration_add_pending_payment_status.sql

# Cách 2: File .bat
run_migration.bat

# Cách 3: phpMyAdmin
# Copy paste SQL và execute
```

## 📝 Notes

- Column `payment_method` là **nullable** (có thể NULL)
- Đơn cũ sẽ được update tự động trong migration
- Frontend đã nhận `paymentMethod` từ response
- Có thể thêm validation nếu cần

Happy coding! 🎉
