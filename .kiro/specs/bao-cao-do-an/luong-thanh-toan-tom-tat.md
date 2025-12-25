# Luồng Thanh Toán Online - Tóm Tắt Ngắn Gọn

## 1. TẠO ĐỚN HÀNG

**Frontend** → `POST /api/orders` với body:
```json
{
  "address": "123 Nguyễn Trãi",
  "ward": "26734",
  "district": "Thanh Xuân", 
  "province": "Hà Nội",
  "paymentMethod": "SEPAY",
  "shippingFee": 30000
}
```

**Backend**:
- `OrderController.createOrder()` nhận request
- Gọi `OrderService.createOrderFromCart()`
- Service tạo order với status = `PENDING_PAYMENT`
- Lưu vào DB: `orders` table
- Trả về response:
```json
{
  "orderId": 123,
  "orderCode": "ORD20231223XXXX",
  "status": "PENDING_PAYMENT",
  "total": 60030000
}
```

---

## 2. TẠO PAYMENT VÀ HIỂN THỊ QR

**Frontend** nhận orderCode → Gọi `POST /api/payment/create` với body:
```json
{
  "orderId": 123,
  "amount": 60030000
}
```

**Backend**:
- `PaymentController.createPayment()` nhận request
- Gọi `PaymentService.createPayment(request, userId)`
- Service làm:
  1. Validate order, user, amount
  2. Generate payment code: `PAY20231223XXXX`
  3. Get bank info từ DB: `bank_accounts` table (is_default = true)
  4. **Generate QR URL**: 
     ```java
     String qrUrl = "https://img.vietqr.io/image/" + 
                    bankCode + "-" + accountNumber + 
                    "-qr_only.jpg?amount=" + amount + 
                    "&addInfo=" + paymentCode;
     ```
  5. Tạo Payment entity với `sepayQrCode = qrUrl`
  6. Lưu vào DB: `payments` table
  7. Update `orders.payment_id = payment.id`

- Trả về response:
```json
{
  "paymentCode": "PAY20231223XXXX",
  "qrCodeUrl": "https://img.vietqr.io/image/MBBank-3333315012003-qr_only.jpg?amount=60030000&addInfo=PAY20231223XXXX",
  "bankCode": "MBBank",
  "accountNumber": "3333315012003",
  "accountName": "CONG TY TNHH TECHMART",
  "content": "PAY20231223XXXX",
  "amount": 60030000,
  "status": "PENDING",
  "expiredAt": "2023-12-23T15:30:00"
}
```

**Frontend**:
- Redirect → `/payment/ORD20231223XXXX`
- Load payment info: `GET /api/payment/order/123`
- Hiển thị: `<img src={payment.qrCodeUrl} />`
- QR code tự động hiển thị (VietQR API generate real-time)

---

## 3. KHÁCH HÀNG THANH TOÁN

**Khách hàng**:
- Quét QR code bằng app ngân hàng
- Chuyển khoản với nội dung: `PAY20231223XXXX`

**Ngân hàng** → **SePay** → Gọi webhook: `POST /api/payment/sepay/webhook`

**Backend**:
- `PaymentController.handleSepayWebhook()` nhận request
- Gọi `PaymentService.handleSepayWebhook(request)`
- Service làm:
  1. Extract payment code từ content: `PAY20231223XXXX`
  2. Tìm payment: `paymentRepository.findByPaymentCode()`
  3. Validate: amount, signature, expiration
  4. **Update payment**:
     - `status = SUCCESS`
     - `sepayTransactionId = "TXN123"`
     - `paidAt = now()`
     - Lưu vào DB: `UPDATE payments SET ...`
  5. **Update order**:
     - `paymentStatus = PAID`
     - `status = CONFIRMED`
     - `confirmedAt = now()`
     - Lưu vào DB: `UPDATE orders SET ...`
  6. Publish event: `OrderStatusChangedEvent`
  7. **Accounting listener** tự động tạo:
     - `financial_transactions` table
     - `type = REVENUE`
     - `category = ONLINE_PAYMENT`
     - `amount = 60030000`

- Trả về SePay: `200 OK`

---

## 4. FRONTEND PHÁT HIỆN THÀNH CÔNG

**Frontend** đang polling (mỗi 15s): `GET /api/payment/PAY20231223XXXX/status`

**Backend**:
- `PaymentController.checkPaymentStatus()` 
- Gọi `PaymentService.checkPaymentStatus()`
- Trả về: `status = "SUCCESS"`

**Frontend**:
- Detect `status === 'SUCCESS'`
- Stop polling
- Toast: "Thanh toán thành công!"
- Redirect → `/orders/ORD20231223XXXX?success=true`

---

## TÓM TẮT DATABASE

### Sau khi tạo order:
```sql
INSERT INTO orders (order_code, status, payment_status, total, ...)
VALUES ('ORD20231223XXXX', 'PENDING_PAYMENT', 'UNPAID', 60030000, ...);
```

### Sau khi tạo payment:
```sql
INSERT INTO payments (payment_code, order_id, amount, status, sepay_qr_code, expired_at, ...)
VALUES ('PAY20231223XXXX', 123, 60030000, 'PENDING', 'https://img.vietqr.io/...', NOW() + INTERVAL 15 MINUTE, ...);

UPDATE orders SET payment_id = 456 WHERE id = 123;
```

### Sau khi thanh toán thành công:
```sql
-- Update payment
UPDATE payments 
SET status = 'SUCCESS', 
    sepay_transaction_id = 'TXN123',
    paid_at = NOW()
WHERE payment_code = 'PAY20231223XXXX';

-- Update order
UPDATE orders 
SET payment_status = 'PAID',
    status = 'CONFIRMED',
    confirmed_at = NOW()
WHERE id = 123;

-- Accounting tự động tạo
INSERT INTO financial_transactions (order_id, type, category, amount, transaction_date)
VALUES (123, 'REVENUE', 'ONLINE_PAYMENT', 60030000, NOW());
```

---

## FLOW NGẮN GỌN

1. **Tạo order** → `OrderController.createOrder()` → `OrderService.createOrderFromCart()` → Lưu `orders` table → Trả `orderCode`

2. **Tạo payment** → `PaymentController.createPayment()` → `PaymentService.createPayment()` → Generate QR URL → Lưu `payments` table → Trả `qrCodeUrl`

3. **Hiển thị QR** → Frontend render `<img src={qrCodeUrl} />` → QR tự động hiển thị

4. **Webhook** → `PaymentController.handleSepayWebhook()` → `PaymentService.handleSepayWebhook()` → Update `payments` (status=SUCCESS) → Update `orders` (status=CONFIRMED) → Tạo `financial_transactions`

5. **Polling** → `PaymentController.checkPaymentStatus()` → `PaymentService.checkPaymentStatus()` → Trả status → Frontend redirect

---

**Document Version**: 1.0  
**Last Updated**: 2024-12-25
