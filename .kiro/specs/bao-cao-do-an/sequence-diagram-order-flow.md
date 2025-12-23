# Sơ Đồ Tuần Tự - Luồng Đặt Hàng (Order Flow)

## Tổng Quan

Tài liệu này mô tả chi tiết các sơ đồ tuần tự (sequence diagrams) cho luồng đặt hàng trong hệ thống TMDT, bao gồm:
- Luồng đặt hàng với thanh toán COD (Cash on Delivery)
- Luồng đặt hàng với thanh toán Online (SePay)
- Xử lý validation và error handling
- Tương tác giữa các components

**Requirements**: 1.1-1.5 (Quản lý đơn hàng), 8.1-8.5 (Thanh toán online)

---

## 1. Luồng Đặt Hàng COD (Cash on Delivery)

### 1.1. Mô Tả

Luồng này xử lý khi khách hàng chọn thanh toán khi nhận hàng (COD). Đơn hàng được tạo với trạng thái PENDING và chờ Sales staff xác nhận.

### 1.2. Các Bước Chính

1. Customer checkout với giỏ hàng
2. Nhập thông tin giao hàng (địa chỉ, số điện thoại)
3. Chọn phương thức thanh toán: COD
4. Hệ thống validate dữ liệu
5. Kiểm tra tồn kho (available quantity)
6. Reserve stock (tăng reserved quantity)
7. Tạo order với status PENDING
8. Trả về order confirmation

### 1.3. Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    actor Customer
    participant UI as Next.js Frontend
    participant API as API Gateway
    participant OrderCtrl as OrderController
    participant OrderSvc as OrderService
    participant InvSvc as InventoryService
    participant CartSvc as CartService
    participant DB as MySQL Database
    
    Note over Customer,DB: Phase 1: Customer Checkout
    Customer->>UI: Click "Đặt hàng" button
    UI->>UI: Validate form data
    
    alt Form validation fails
        UI-->>Customer: Show validation errors
    else Form valid
        UI->>API: POST /api/orders/create
        Note right of UI: Request body:<br/>{<br/>  items: [{productId, quantity}],<br/>  shippingAddress: {...},<br/>  paymentMethod: "COD",<br/>  phone: "..."<br/>}
        
        API->>OrderCtrl: createOrder(OrderRequest)
        activate OrderCtrl
        
        Note over OrderCtrl,DB: Phase 2: Validation & Stock Check
        OrderCtrl->>OrderCtrl: Validate request data
        OrderCtrl->>OrderSvc: createOrder(request)
        activate OrderSvc
        
        OrderSvc->>OrderSvc: Validate shipping address
        Note right of OrderSvc: Check required fields:<br/>- Province<br/>- District<br/>- Ward<br/>- Detailed address
        
        alt Address validation fails
            OrderSvc-->>OrderCtrl: ValidationException
            OrderCtrl-->>API: 400 Bad Request
            API-->>UI: Error response
            UI-->>Customer: "Địa chỉ không hợp lệ"
        else Address valid
            
            OrderSvc->>InvSvc: checkStockAvailability(items)
            activate InvSvc
            
            loop For each item
                InvSvc->>DB: SELECT available_quantity<br/>FROM inventory_stock<br/>WHERE product_id = ?
                DB-->>InvSvc: Stock data
                InvSvc->>InvSvc: Check if available >= required
            end
            
            alt Insufficient stock
                InvSvc-->>OrderSvc: InsufficientStockException
                Note right of InvSvc: Return details:<br/>- Product name<br/>- Required quantity<br/>- Available quantity
                OrderSvc-->>OrderCtrl: StockException
                OrderCtrl-->>API: 400 Bad Request
                API-->>UI: Error with stock details
                UI-->>Customer: "Sản phẩm X không đủ hàng<br/>(Cần: 5, Còn: 2)"
            else Stock available
                InvSvc-->>OrderSvc: Stock check passed
                deactivate InvSvc
                
                Note over OrderSvc,DB: Phase 3: Reserve Stock & Create Order
                OrderSvc->>InvSvc: reserveStock(items)
                activate InvSvc
                
                loop For each item
                    InvSvc->>DB: UPDATE inventory_stock<br/>SET reserved = reserved + ?<br/>WHERE product_id = ?
                    Note right of DB: available = onHand - reserved - damaged<br/>This decreases available quantity
                end
                
                InvSvc-->>OrderSvc: Stock reserved
                deactivate InvSvc
                
                OrderSvc->>OrderSvc: Generate unique order code
                Note right of OrderSvc: Format: ORD-YYYYMMDD-XXXXX
                
                OrderSvc->>OrderSvc: Calculate total amount
                Note right of OrderSvc: total = sum(item.price * item.quantity)<br/>+ shippingFee
                
                OrderSvc->>DB: BEGIN TRANSACTION
                
                OrderSvc->>DB: INSERT INTO orders<br/>(order_code, customer_id, status,<br/>payment_method, total, shipping_address)
                Note right of DB: status = 'PENDING'<br/>payment_method = 'COD'<br/>payment_status = 'UNPAID'
                DB-->>OrderSvc: Order ID
                
                loop For each item
                    OrderSvc->>DB: INSERT INTO order_items<br/>(order_id, product_id, quantity, price)
                end
                
                OrderSvc->>CartSvc: clearCart(customerId)
                activate CartSvc
                CartSvc->>DB: DELETE FROM cart_items<br/>WHERE customer_id = ?
                deactivate CartSvc
                
                OrderSvc->>DB: COMMIT TRANSACTION
                
                Note over OrderSvc,DB: Phase 4: Return Response
                OrderSvc->>DB: SELECT order with items
                DB-->>OrderSvc: Complete order data
                
                OrderSvc-->>OrderCtrl: OrderResponse
                deactivate OrderSvc
                OrderCtrl-->>API: 200 OK
                deactivate OrderCtrl
                API-->>UI: Order created successfully
                UI-->>Customer: Show order confirmation<br/>"Đơn hàng #ORD-20231223-00001<br/>đã được tạo thành công"
            end
        end
    end
```

### 1.4. Validation Rules

#### Shipping Address Validation
- **Province**: Required, must exist in province table
- **District**: Required, must exist in district table and belong to selected province
- **Ward**: Required, must exist in ward table and belong to selected district
- **Detailed Address**: Required, min length 10 characters
- **Phone**: Required, format: 10 digits starting with 0

#### Stock Validation
- **Available Quantity**: Must be >= requested quantity for each item
- **Calculation**: available = onHand - reserved - damaged
- **Atomic Check**: All items must pass stock check before creating order

#### Business Rules
- **Order Code**: Unique, auto-generated, format: ORD-YYYYMMDD-XXXXX
- **Status**: New COD orders start with PENDING status
- **Payment Status**: UNPAID for COD orders
- **Stock Reservation**: Reserved quantity increases immediately upon order creation
- **Cart Clearing**: Cart is cleared only after successful order creation

### 1.5. Error Handling

| Error Type | HTTP Code | Error Message | User Action |
|------------|-----------|---------------|-------------|
| Invalid Address | 400 | "Địa chỉ giao hàng không hợp lệ" | Re-enter address |
| Insufficient Stock | 400 | "Sản phẩm X không đủ hàng (Cần: Y, Còn: Z)" | Reduce quantity or remove item |
| Invalid Phone | 400 | "Số điện thoại không hợp lệ" | Re-enter phone number |
| Database Error | 500 | "Lỗi hệ thống, vui lòng thử lại" | Retry or contact support |
| Network Timeout | 504 | "Kết nối timeout, vui lòng thử lại" | Retry |

---

## 2. Luồng Đặt Hàng Online Payment (SePay)

### 2.1. Mô Tả

Luồng này xử lý khi khách hàng chọn thanh toán online qua SePay. Hệ thống tạo đơn hàng với trạng thái PENDING_PAYMENT, generate QR code, và chờ webhook từ SePay để xác nhận thanh toán.

### 2.2. Các Bước Chính

1. Customer checkout và chọn Online Payment
2. Hệ thống tạo order với status PENDING_PAYMENT
3. Generate QR code từ SePay API
4. Customer quét QR và chuyển khoản
5. SePay gửi webhook notification
6. Hệ thống verify webhook và match payment
7. Update order status thành CONFIRMED
8. Ghi nhận bút toán kế toán

### 2.3. Sơ Đồ Tuần Tự - Tạo Đơn và Generate QR

```mermaid
sequenceDiagram
    actor Customer
    participant UI as Next.js Frontend
    participant API as API Gateway
    participant OrderCtrl as OrderController
    participant OrderSvc as OrderService
    participant PaymentSvc as PaymentService
    participant InvSvc as InventoryService
    participant SePay as SePay API
    participant DB as MySQL Database
    
    Note over Customer,DB: Phase 1: Create Order with Online Payment
    Customer->>UI: Select "Thanh toán online"
    UI->>API: POST /api/orders/create
    Note right of UI: paymentMethod: "ONLINE"
    
    API->>OrderCtrl: createOrder(OrderRequest)
    activate OrderCtrl
    
    OrderCtrl->>OrderSvc: createOrder(request)
    activate OrderSvc
    
    Note over OrderSvc,DB: Validation & Stock Check (same as COD)
    OrderSvc->>OrderSvc: Validate shipping address
    OrderSvc->>InvSvc: checkStockAvailability(items)
    activate InvSvc
    InvSvc->>DB: Check available quantity
    DB-->>InvSvc: Stock data
    
    alt Insufficient stock
        InvSvc-->>OrderSvc: InsufficientStockException
        OrderSvc-->>OrderCtrl: Error
        OrderCtrl-->>UI: 400 Bad Request
        UI-->>Customer: Stock error message
    else Stock available
        InvSvc-->>OrderSvc: OK
        deactivate InvSvc
        
        Note over OrderSvc,DB: Phase 2: Create Order & Payment Record
        OrderSvc->>InvSvc: reserveStock(items)
        
        OrderSvc->>DB: BEGIN TRANSACTION
        
        OrderSvc->>DB: INSERT INTO orders<br/>(order_code, status, payment_method)
        Note right of DB: status = 'PENDING_PAYMENT'<br/>payment_method = 'ONLINE'<br/>payment_status = 'UNPAID'
        DB-->>OrderSvc: Order ID
        
        OrderSvc->>DB: INSERT INTO order_items
        
        Note over OrderSvc,PaymentSvc: Phase 3: Generate Payment QR Code
        OrderSvc->>PaymentSvc: createPayment(orderId, amount)
        activate PaymentSvc
        
        PaymentSvc->>PaymentSvc: Generate payment code
        Note right of PaymentSvc: Format: PAY-YYYYMMDD-XXXXX
        
        PaymentSvc->>DB: INSERT INTO payments<br/>(payment_code, order_id, amount,<br/>status, expired_at)
        Note right of DB: status = 'PENDING'<br/>expired_at = now + 15 minutes
        DB-->>PaymentSvc: Payment ID
        
        PaymentSvc->>SePay: POST /api/v1/qr-code/generate
        activate SePay
        Note right of PaymentSvc: Request:<br/>{<br/>  accountNumber: "...",<br/>  amount: 500000,<br/>  description: "PAY-20231223-00001"<br/>}
        
        alt SePay API error
            SePay-->>PaymentSvc: 500 Error
            PaymentSvc-->>OrderSvc: PaymentException
            OrderSvc->>DB: ROLLBACK TRANSACTION
            OrderSvc-->>OrderCtrl: Error
            OrderCtrl-->>UI: 500 Internal Error
            UI-->>Customer: "Lỗi tạo QR code, vui lòng thử lại"
        else SePay success
            SePay-->>PaymentSvc: QR code data
            deactivate SePay
            Note right of SePay: Response:<br/>{<br/>  qrCode: "base64...",<br/>  qrDataURL: "https://..."<br/>}
            
            PaymentSvc->>DB: UPDATE payments<br/>SET qr_code = ?, qr_data_url = ?
            
            PaymentSvc-->>OrderSvc: PaymentResponse with QR
            deactivate PaymentSvc
            
            OrderSvc->>DB: COMMIT TRANSACTION
            
            OrderSvc-->>OrderCtrl: OrderResponse + PaymentResponse
            deactivate OrderSvc
            OrderCtrl-->>API: 200 OK
            deactivate OrderCtrl
            
            Note over UI,Customer: Phase 4: Display QR Code
            API-->>UI: Order + Payment data
            UI->>UI: Render QR code image
            UI->>UI: Start countdown timer (15 minutes)
            UI-->>Customer: Show QR code<br/>"Quét mã QR để thanh toán<br/>Số tiền: 500,000 VNĐ<br/>Hết hạn sau: 14:59"
            
            Customer->>Customer: Open banking app
            Customer->>Customer: Scan QR code
            Customer->>Customer: Confirm transfer
        end
    end
```

### 2.4. Sơ Đồ Tuần Tự - Xử Lý Webhook SePay

```mermaid
sequenceDiagram
    participant Bank as Customer's Bank
    participant SePay as SePay System
    participant Webhook as WebhookController
    participant WebhookSvc as WebhookService
    participant PaymentSvc as PaymentService
    participant OrderSvc as OrderService
    participant AcctSvc as AccountingService
    participant DB as MySQL Database
    participant UI as Frontend (Polling)
    actor Customer
    
    Note over Bank,Customer: Phase 1: Customer Completes Transfer
    Customer->>Bank: Transfer money via QR
    Bank->>Bank: Process transaction
    Bank->>SePay: Transaction notification
    
    Note over SePay,DB: Phase 2: SePay Webhook
    SePay->>Webhook: POST /api/webhook/sepay
    activate Webhook
    Note right of SePay: Request body:<br/>{<br/>  transactionId: "...",<br/>  amount: 500000,<br/>  description: "PAY-20231223-00001",<br/>  transactionDate: "...",<br/>  signature: "..."<br/>}
    
    Webhook->>WebhookSvc: processSepayWebhook(request)
    activate WebhookSvc
    
    Note over WebhookSvc: Phase 3: Verify Webhook Signature
    WebhookSvc->>WebhookSvc: verifySignature(request)
    Note right of WebhookSvc: Calculate HMAC-SHA256<br/>using secret key<br/>Compare with signature
    
    alt Invalid signature
        WebhookSvc-->>Webhook: SecurityException
        Webhook-->>SePay: 401 Unauthorized
        Note right of Webhook: SePay will retry later
    else Valid signature
        
        Note over WebhookSvc,DB: Phase 4: Check Duplicate
        WebhookSvc->>DB: SELECT FROM payments<br/>WHERE transaction_id = ?
        DB-->>WebhookSvc: Existing payment (if any)
        
        alt Duplicate transaction
            WebhookSvc-->>Webhook: Already processed
            Webhook-->>SePay: 200 OK (idempotent)
            Note right of Webhook: Return success to prevent retry
        else New transaction
            
            Note over WebhookSvc,DB: Phase 5: Match Payment
            WebhookSvc->>WebhookSvc: Extract payment code from description
            Note right of WebhookSvc: Parse "PAY-20231223-00001"<br/>from description field
            
            WebhookSvc->>DB: SELECT FROM payments<br/>WHERE payment_code = ?
            DB-->>WebhookSvc: Payment record
            
            alt Payment not found
                WebhookSvc->>DB: INSERT INTO unmatched_payments<br/>(transaction_id, amount, description)
                Note right of DB: Store for manual review
                WebhookSvc-->>Webhook: Payment not found
                Webhook-->>SePay: 200 OK
                Note right of Webhook: Still return 200 to prevent retry
            else Payment found
                
                Note over WebhookSvc,DB: Phase 6: Verify Amount
                WebhookSvc->>WebhookSvc: Compare amounts
                
                alt Amount mismatch
                    WebhookSvc->>DB: UPDATE payments<br/>SET status = 'AMOUNT_MISMATCH',<br/>actual_amount = ?
                    WebhookSvc->>DB: INSERT INTO payment_issues<br/>(payment_id, issue_type, description)
                    Note right of DB: Flag for manual review
                    WebhookSvc-->>Webhook: Amount mismatch
                    Webhook-->>SePay: 200 OK
                else Amount matches
                    
                    Note over WebhookSvc,DB: Phase 7: Update Payment & Order
                    WebhookSvc->>DB: BEGIN TRANSACTION
                    
                    WebhookSvc->>DB: UPDATE payments<br/>SET status = 'COMPLETED',<br/>transaction_id = ?,<br/>paid_at = NOW()
                    
                    WebhookSvc->>PaymentSvc: confirmPayment(paymentId)
                    activate PaymentSvc
                    
                    PaymentSvc->>DB: SELECT order_id FROM payments
                    DB-->>PaymentSvc: Order ID
                    
                    PaymentSvc->>OrderSvc: updatePaymentStatus(orderId, PAID)
                    activate OrderSvc
                    
                    OrderSvc->>DB: UPDATE orders<br/>SET payment_status = 'PAID',<br/>status = 'CONFIRMED',<br/>confirmed_at = NOW()
                    Note right of DB: Auto-confirm order<br/>when payment received
                    
                    OrderSvc-->>PaymentSvc: Order updated
                    deactivate OrderSvc
                    
                    PaymentSvc-->>WebhookSvc: Payment confirmed
                    deactivate PaymentSvc
                    
                    Note over WebhookSvc,AcctSvc: Phase 8: Record Accounting Entry
                    WebhookSvc->>AcctSvc: recordPayment(paymentId)
                    activate AcctSvc
                    
                    AcctSvc->>DB: INSERT INTO financial_transaction<br/>(type, category, amount, order_id)
                    Note right of DB: type = 'REVENUE'<br/>category = 'ONLINE_PAYMENT'<br/>amount = 500000
                    
                    AcctSvc-->>WebhookSvc: Accounting recorded
                    deactivate AcctSvc
                    
                    WebhookSvc->>DB: COMMIT TRANSACTION
                    
                    WebhookSvc-->>Webhook: Success
                    deactivate WebhookSvc
                    Webhook-->>SePay: 200 OK
                    deactivate Webhook
                    
                    Note over UI,Customer: Phase 9: Frontend Polling
                    UI->>UI: Poll payment status every 3 seconds
                    UI->>Webhook: GET /api/payments/{paymentCode}/status
                    Webhook->>DB: SELECT status FROM payments
                    DB-->>Webhook: status = 'COMPLETED'
                    Webhook-->>UI: Payment completed
                    
                    UI->>UI: Stop polling
                    UI->>UI: Redirect to success page
                    UI-->>Customer: "Thanh toán thành công!<br/>Đơn hàng đã được xác nhận"
                end
            end
        end
    end
```

### 2.5. Payment Timeout Handling

```mermaid
sequenceDiagram
    participant Scheduler as Spring Scheduler
    participant PaymentSvc as PaymentService
    participant OrderSvc as OrderService
    participant InvSvc as InventoryService
    participant DB as MySQL Database
    
    Note over Scheduler,DB: Runs every 5 minutes
    Scheduler->>PaymentSvc: checkExpiredPayments()
    activate PaymentSvc
    
    PaymentSvc->>DB: SELECT FROM payments<br/>WHERE status = 'PENDING'<br/>AND expired_at < NOW()
    DB-->>PaymentSvc: List of expired payments
    
    loop For each expired payment
        PaymentSvc->>DB: BEGIN TRANSACTION
        
        PaymentSvc->>DB: UPDATE payments<br/>SET status = 'EXPIRED'
        
        PaymentSvc->>DB: SELECT order_id FROM payments
        DB-->>PaymentSvc: Order ID
        
        PaymentSvc->>OrderSvc: cancelOrder(orderId, "Payment timeout")
        activate OrderSvc
        
        OrderSvc->>DB: UPDATE orders<br/>SET status = 'CANCELLED',<br/>cancel_reason = 'Payment timeout'
        
        OrderSvc->>InvSvc: releaseReservedStock(orderId)
        activate InvSvc
        
        InvSvc->>DB: UPDATE inventory_stock<br/>SET reserved = reserved - ?<br/>WHERE product_id IN (...)
        Note right of DB: Restore available quantity
        
        InvSvc-->>OrderSvc: Stock released
        deactivate InvSvc
        
        OrderSvc-->>PaymentSvc: Order cancelled
        deactivate OrderSvc
        
        PaymentSvc->>DB: COMMIT TRANSACTION
        
        Note right of PaymentSvc: Optional: Send email notification
    end
    
    deactivate PaymentSvc
```

### 2.6. Business Rules - Online Payment

#### Payment Expiration
- **Timeout**: 15 minutes from order creation
- **Scheduler**: Runs every 5 minutes to check expired payments
- **Action**: Auto-cancel order and release reserved stock

#### Webhook Security
- **Signature Verification**: HMAC-SHA256 with secret key
- **Idempotency**: Check transaction_id to prevent duplicate processing
- **Response**: Always return 200 OK to prevent unnecessary retries

#### Payment Matching
- **Primary Key**: Payment code in transfer description (e.g., "PAY-20231223-00001")
- **Fallback**: Manual matching by admin if description is incorrect
- **Amount Verification**: Must match exactly, otherwise flag for review

#### Order Status Transition
- **On Payment Success**: PENDING_PAYMENT → CONFIRMED
- **On Payment Timeout**: PENDING_PAYMENT → CANCELLED
- **Stock Release**: Automatic when order is cancelled

---

## 3. Validation và Error Handling

### 3.1. Validation Points

```mermaid
graph TD
    A[Order Request] --> B{Form Validation}
    B -->|Invalid| C[Return 400 with field errors]
    B -->|Valid| D{Address Validation}
    D -->|Invalid| E[Return 400 with address errors]
    D -->|Valid| F{Stock Check}
    F -->|Insufficient| G[Return 400 with stock details]
    F -->|Available| H{Payment Method}
    H -->|COD| I[Create Order - PENDING]
    H -->|Online| J{Generate QR}
    J -->|SePay Error| K[Return 500 - Retry]
    J -->|Success| L[Create Order - PENDING_PAYMENT]
    I --> M[Return Order Response]
    L --> M
```

### 3.2. Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Không đủ hàng để đặt",
    "details": [
      {
        "productId": 123,
        "productName": "iPhone 15 Pro Max",
        "required": 5,
        "available": 2
      }
    ]
  },
  "timestamp": "2023-12-23T10:30:00Z"
}
```

### 3.3. Retry Strategy

| Scenario | Retry | Strategy |
|----------|-------|----------|
| Stock check fails | Yes | Immediate retry (user action) |
| SePay QR generation fails | Yes | Exponential backoff (3 attempts) |
| Webhook processing fails | Yes | SePay auto-retry (5 times, 5 min interval) |
| Database timeout | Yes | Spring @Retryable (3 attempts) |
| Payment timeout | No | Auto-cancel after 15 minutes |

---

## 4. Performance Considerations

### 4.1. Database Optimization

- **Indexes**: 
  - `orders(order_code)` - UNIQUE index for fast lookup
  - `orders(customer_id, status)` - Composite index for customer order list
  - `payments(payment_code)` - UNIQUE index for webhook matching
  - `inventory_stock(product_id)` - Index for stock checks

- **Transaction Isolation**: 
  - Use `READ_COMMITTED` for order creation
  - Use `SERIALIZABLE` for stock reservation to prevent race conditions

### 4.2. Concurrency Handling

```sql
-- Pessimistic locking for stock reservation
SELECT * FROM inventory_stock 
WHERE product_id = ? 
FOR UPDATE;

UPDATE inventory_stock 
SET reserved = reserved + ? 
WHERE product_id = ?;
```

### 4.3. Caching Strategy (Future Enhancement)

- Cache product information (price, name) for 5 minutes
- Cache province/district/ward data (rarely changes)
- Do NOT cache inventory stock (real-time data)

---

## 5. Monitoring và Logging

### 5.1. Key Metrics

- **Order Creation Rate**: Orders per minute
- **Payment Success Rate**: Successful payments / Total payments
- **Stock Check Latency**: Average time for stock validation
- **Webhook Processing Time**: Time from webhook receipt to order update

### 5.2. Log Events

```java
// Order creation
log.info("Order created: orderCode={}, customerId={}, total={}, paymentMethod={}", 
    orderCode, customerId, total, paymentMethod);

// Stock reservation
log.info("Stock reserved: productId={}, quantity={}, remainingAvailable={}", 
    productId, quantity, remainingAvailable);

// Payment webhook
log.info("Payment webhook received: transactionId={}, amount={}, paymentCode={}", 
    transactionId, amount, paymentCode);

// Payment matched
log.info("Payment matched: paymentCode={}, orderId={}, status={}", 
    paymentCode, orderId, newStatus);

// Error cases
log.error("Insufficient stock: productId={}, required={}, available={}", 
    productId, required, available);
log.error("Payment amount mismatch: expected={}, actual={}, paymentCode={}", 
    expected, actual, paymentCode);
```

---

## 6. Kết Luận

### 6.1. Điểm Mạnh

1. **Validation Toàn Diện**: Kiểm tra dữ liệu ở nhiều tầng (frontend, backend, database)
2. **Stock Management**: Reserve stock ngay khi tạo đơn để tránh overselling
3. **Payment Security**: Webhook signature verification, idempotency check
4. **Error Handling**: Chi tiết, user-friendly error messages
5. **Transaction Safety**: ACID compliance với database transactions

### 6.2. Trade-offs

1. **Stock Reservation**: Reserved stock có thể bị "lock" nếu customer không thanh toán
   - **Mitigation**: Auto-cancel sau 15 phút cho online payment
   
2. **Webhook Dependency**: Phụ thuộc vào SePay webhook cho payment confirmation
   - **Mitigation**: Polling mechanism trên frontend, manual matching option

3. **Database Load**: Multiple queries cho stock check và validation
   - **Mitigation**: Indexes, connection pooling, future caching

### 6.3. Future Improvements

1. **Async Processing**: Use message queue (RabbitMQ/Kafka) for webhook processing
2. **Caching**: Redis cache for product data and address data
3. **Rate Limiting**: Prevent abuse of order creation endpoint
4. **Notification**: Email/SMS notification for order status changes
5. **Analytics**: Real-time dashboard for order metrics

---

**Document Version**: 1.0  
**Last Updated**: 2023-12-23  
**Author**: System Analysis Team
