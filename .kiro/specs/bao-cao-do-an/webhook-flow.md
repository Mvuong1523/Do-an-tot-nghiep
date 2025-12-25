# Webhook Flow - SePay Bắn Về Đâu và Cập Nhật Thế Nào

## 1. WEBHOOK BẮN VỀ ĐÂU?

### URL Webhook:
```
POST http://localhost:8080/api/payment/sepay/webhook
```

**Trong production**:
```
POST https://yourdomain.com/api/payment/sepay/webhook
```

### Ai gọi webhook?
- **SePay System** tự động gọi khi phát hiện có giao dịch chuyển khoản thành công
- **Không cần auth** (public endpoint)
- SePay gửi kèm signature để verify

### Khi nào gọi?
- Ngay sau khi khách hàng chuyển khoản thành công
- Ngân hàng → SePay → Webhook của bạn (trong vài giây)

---

## 2. WEBHOOK NHẬN GÌ?

### Request từ SePay:

**Endpoint**: `PaymentController.java`
```java
@PostMapping("/sepay/webhook")
public ApiResponse handleSepayWebhook(@RequestBody SepayWebhookRequest request) {
    log.info("Received SePay webhook for payment: {}", request.getContent());
    return paymentService.handleSepayWebhook(request);
}
```

**Body nhận được** (`SepayWebhookRequest`):
```json
{
  "id": "TXN123456789",                    // Transaction ID từ SePay
  "gateway": "MBBank",                     // Ngân hàng
  "accountNumber": "3333315012003",        // Số tài khoản nhận
  "transferAmount": 60030000,              // Số tiền
  "content": "PAY20231223XXXX FT2533",     // Nội dung CK (có payment code)
  "transactionDate": "2023-12-23 14:25:30",
  "status": "SUCCESS",
  "signature": "abc123xyz..."              // Chữ ký để verify
}
```

---

## 3. CẬP NHẬT TRẠNG THÁI Ở ĐÂU?

### Bước 1: PaymentController nhận webhook

**File**: `src/main/java/com/doan/WEB_TMDT/module/payment/controller/PaymentController.java`

**Method**: `handleSepayWebhook()`

```java
@PostMapping("/sepay/webhook")
public ApiResponse handleSepayWebhook(@RequestBody SepayWebhookRequest request) {
    log.info("Received SePay webhook for payment: {}", request.getContent());
    return paymentService.handleSepayWebhook(request);
}
```

**Đặc điểm**:
- **Không có @PreAuthorize** → Public, không cần login
- SePay gọi được từ bên ngoài
- Log lại để debug

---

### Bước 2: PaymentService xử lý webhook

**File**: `src/main/java/com/doan/WEB_TMDT/module/payment/service/impl/PaymentServiceImpl.java`

**Method**: `handleSepayWebhook()`

**Annotation**: `@Transactional` (đảm bảo atomicity)

#### 2.1. Extract Payment Code
```java
String content = request.getContent(); // "PAY20231223XXXX FT2533"
String paymentCode = extractPaymentCode(content); // "PAY20231223XXXX"
```

#### 2.2. Tìm Payment trong DB
```java
Payment payment = paymentRepository.findByPaymentCode(paymentCode)
    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));
```

**SQL**:
```sql
SELECT * FROM payments WHERE payment_code = 'PAY20231223XXXX';
```

#### 2.3. Validate
```java
// Check đã xử lý chưa
if (payment.getStatus() == PaymentStatus.SUCCESS) {
    return ApiResponse.success("Thanh toán đã được xử lý");
}

// Check số tiền
if (!payment.getAmount().equals(request.getAmount())) {
    return ApiResponse.error("Số tiền không khớp");
}

// Check hết hạn chưa
if (LocalDateTime.now().isAfter(payment.getExpiredAt())) {
    payment.setStatus(PaymentStatus.EXPIRED);
    return ApiResponse.error("Thanh toán đã hết hạn");
}
```

---

### Bước 3: CẬP NHẬT PAYMENT (Transaction)

**Vị trí**: `PaymentServiceImpl.handleSepayWebhook()`

**Code**:
```java
// Update payment entity
payment.setStatus(PaymentStatus.SUCCESS);
payment.setSepayTransactionId(request.getTransactionId());
payment.setPaidAt(LocalDateTime.now());
payment.setSepayResponse(request.toString());
paymentRepository.save(payment);
```

**SQL thực thi**:
```sql
UPDATE payments 
SET status = 'SUCCESS',
    sepay_transaction_id = 'TXN123456789',
    paid_at = '2023-12-23 14:25:30',
    sepay_response = '{"id":"TXN123456789","gateway":"MBBank",...}'
WHERE id = 456;
```

**Repository**: `PaymentRepository.java`
- Method: `save(payment)`
- JPA tự động generate UPDATE query

---

### Bước 4: CẬP NHẬT ORDER (Transaction)

**Vị trí**: `PaymentServiceImpl.handleSepayWebhook()`

**Code**:
```java
// Get order from payment
Order order = payment.getOrder();
OrderStatus oldStatus = order.getStatus(); // PENDING_PAYMENT

// Update order
order.setPaymentStatus(PaymentStatus.PAID);
order.setStatus(OrderStatus.CONFIRMED);
order.setConfirmedAt(LocalDateTime.now());
orderRepository.save(order);
```

**SQL thực thi**:
```sql
UPDATE orders 
SET payment_status = 'PAID',
    status = 'CONFIRMED',
    confirmed_at = '2023-12-23 14:25:30'
WHERE id = 123;
```

**Repository**: `OrderRepository.java`
- Method: `save(order)`
- JPA tự động generate UPDATE query

---

### Bước 5: PUBLISH EVENT cho Accounting

**Vị trí**: `PaymentServiceImpl.handleSepayWebhook()`

**Code**:
```java
try {
    OrderStatusChangedEvent event = new OrderStatusChangedEvent(
        this, order, oldStatus, order.getStatus()
    );
    eventPublisher.publishEvent(event);
    log.info("Published OrderStatusChangedEvent for order: {}", order.getOrderCode());
} catch (Exception e) {
    log.error("Failed to publish event", e);
    // Không fail payment process nếu event lỗi
}
```

**Event Class**: `OrderStatusChangedEvent.java`
```java
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
}
```

---

### Bước 6: ACCOUNTING LISTENER tự động xử lý

**File**: `src/main/java/com/doan/WEB_TMDT/module/accounting/listener/AccountingEventListener.java`

**Method**: `handleOrderStatusChanged()`

**Annotation**: `@EventListener`, `@Async`

**Code**:
```java
@EventListener
@Async
public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
    Order order = event.getOrder();
    OrderStatus newStatus = event.getNewStatus();
    
    if (newStatus == OrderStatus.CONFIRMED) {
        // Tạo financial transaction cho doanh thu
        accountingService.recordOrderRevenue(order);
    }
}
```

---

### Bước 7: TẠO FINANCIAL TRANSACTION

**File**: `AccountingServiceImpl.java`

**Method**: `recordOrderRevenue()`

**Code**:
```java
public void recordOrderRevenue(Order order) {
    FinancialTransaction transaction = FinancialTransaction.builder()
        .order(order)
        .type(TransactionType.REVENUE)
        .category(TransactionCategory.ONLINE_PAYMENT)
        .amount(order.getTotal())
        .transactionDate(LocalDate.now())
        .description("Doanh thu từ đơn hàng " + order.getOrderCode())
        .build();
    
    financialTransactionRepository.save(transaction);
}
```

**SQL thực thi**:
```sql
INSERT INTO financial_transactions (
    order_id, type, category, amount, transaction_date, description, created_at
) VALUES (
    123, 'REVENUE', 'ONLINE_PAYMENT', 60030000, '2023-12-23',
    'Doanh thu từ đơn hàng ORD20231223XXXX', NOW()
);
```

---

## 4. TÓM TẮT FLOW CẬP NHẬT

### Webhook bắn về:
```
SePay → POST http://localhost:8080/api/payment/sepay/webhook
```

### Cập nhật ở đâu:

1. **PaymentController.handleSepayWebhook()** 
   - Nhận request từ SePay
   - Gọi service

2. **PaymentService.handleSepayWebhook()**
   - Extract payment code
   - Tìm payment trong DB
   - Validate

3. **Update Payment** (trong transaction)
   - `paymentRepository.save(payment)`
   - SQL: `UPDATE payments SET status='SUCCESS', paid_at=NOW() ...`

4. **Update Order** (trong transaction)
   - `orderRepository.save(order)`
   - SQL: `UPDATE orders SET status='CONFIRMED', payment_status='PAID' ...`

5. **Publish Event**
   - `eventPublisher.publishEvent(OrderStatusChangedEvent)`

6. **Accounting Listener** (async)
   - `AccountingEventListener.handleOrderStatusChanged()`
   - Gọi `accountingService.recordOrderRevenue()`

7. **Create Financial Transaction**
   - `financialTransactionRepository.save(transaction)`
   - SQL: `INSERT INTO financial_transactions ...`

---

## 5. DATABASE CHANGES

### Trước khi webhook:
```sql
-- payments table
id=456, payment_code='PAY20231223XXXX', status='PENDING', paid_at=NULL

-- orders table  
id=123, order_code='ORD20231223XXXX', status='PENDING_PAYMENT', payment_status='UNPAID'

-- financial_transactions table
(empty - chưa có record)
```

### Sau khi webhook:
```sql
-- payments table
id=456, payment_code='PAY20231223XXXX', status='SUCCESS', 
paid_at='2023-12-23 14:25:30', sepay_transaction_id='TXN123456789'

-- orders table
id=123, order_code='ORD20231223XXXX', status='CONFIRMED', 
payment_status='PAID', confirmed_at='2023-12-23 14:25:30'

-- financial_transactions table
id=789, order_id=123, type='REVENUE', category='ONLINE_PAYMENT',
amount=60030000, transaction_date='2023-12-23'
```

---

## 6. SETUP WEBHOOK URL

### Development (Local):
Dùng **ngrok** để expose localhost:
```bash
ngrok http 8080
```

Ngrok cho URL: `https://abc123.ngrok.io`

Webhook URL: `https://abc123.ngrok.io/api/payment/sepay/webhook`

### Production:
Webhook URL: `https://yourdomain.com/api/payment/sepay/webhook`

### Config trên SePay Dashboard:
1. Login vào SePay
2. Vào Settings → Webhook
3. Nhập URL: `https://yourdomain.com/api/payment/sepay/webhook`
4. Save

---

## 7. SECURITY

### Verify Signature:
```java
private boolean verifySignature(SepayWebhookRequest request, String apiToken) {
    // TODO: Implement signature verification
    // String data = request.getTransactionId() + request.getAmount() + apiToken;
    // String calculatedSignature = sha256(data);
    // return calculatedSignature.equals(request.getSignature());
    
    return true; // Tạm thời skip cho demo
}
```

### Check trong handleSepayWebhook():
```java
BankAccount bankAccount = bankAccountRepository.findByIsDefaultTrue().orElse(null);

if (bankAccount != null && bankAccount.getSepayApiToken() != null) {
    if (!verifySignature(request, bankAccount.getSepayApiToken())) {
        return ApiResponse.error("Chữ ký không hợp lệ");
    }
}
```

---

**Document Version**: 1.0  
**Last Updated**: 2024-12-25
