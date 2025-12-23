# Task 4 Completion Summary - Sơ Đồ Tuần Tự Luồng Đặt Hàng

## Task Overview

**Task**: Tạo sơ đồ tuần tự cho luồng đặt hàng  
**Status**: ✅ COMPLETED  
**Requirements**: 1.1-1.5 (Quản lý đơn hàng), 8.1-8.5 (Thanh toán online)

## Deliverables

### Main Document
- **File**: `.kiro/specs/bao-cao-do-an/sequence-diagram-order-flow.md`
- **Content**: Comprehensive sequence diagrams for order flow

## What Was Completed

### 1. Luồng Đặt Hàng COD (Cash on Delivery)

#### Sequence Diagram Coverage
- ✅ Customer checkout process
- ✅ Form validation (frontend & backend)
- ✅ Shipping address validation (province/district/ward)
- ✅ Stock availability check
- ✅ Stock reservation mechanism
- ✅ Order creation with PENDING status
- ✅ Cart clearing after successful order
- ✅ Error handling for all failure scenarios

#### Key Components Documented
- **Frontend**: Next.js UI, form validation
- **Backend**: OrderController, OrderService, InventoryService, CartService
- **Database**: MySQL transactions, ACID compliance
- **Validation Rules**: Address format, phone number, stock availability
- **Error Handling**: Detailed error messages with HTTP status codes

#### Business Rules Explained
- Order code generation: `ORD-YYYYMMDD-XXXXX`
- Status: New COD orders start with `PENDING`
- Payment status: `UNPAID` for COD
- Stock reservation: Increases `reserved` quantity immediately
- Available calculation: `available = onHand - reserved - damaged`

### 2. Luồng Đặt Hàng Online Payment (SePay)

#### Sequence Diagram Coverage
- ✅ Order creation with PENDING_PAYMENT status
- ✅ QR code generation via SePay API
- ✅ Payment record creation with expiration time
- ✅ Customer bank transfer process
- ✅ SePay webhook processing
- ✅ Webhook signature verification
- ✅ Payment matching by payment code
- ✅ Amount verification
- ✅ Order status update to CONFIRMED
- ✅ Accounting entry creation
- ✅ Frontend polling mechanism
- ✅ Payment timeout handling (scheduler job)

#### Key Components Documented
- **Payment Service**: QR generation, payment matching
- **Webhook Service**: Signature verification, idempotency check
- **Accounting Service**: Automatic revenue recognition
- **Scheduler**: Auto-cancel expired payments (15 minutes)
- **Frontend**: QR display, countdown timer, status polling

#### Security Measures
- **Webhook Verification**: HMAC-SHA256 signature
- **Idempotency**: Check transaction_id to prevent duplicates
- **Amount Matching**: Exact amount verification
- **Timeout Protection**: Auto-cancel after 15 minutes

### 3. Validation và Error Handling

#### Validation Points Documented
- Form validation (frontend)
- Address validation (backend)
- Stock availability check
- Payment method validation
- Amount verification (online payment)
- Signature verification (webhook)

#### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Không đủ hàng để đặt",
    "details": [...]
  }
}
```

#### Retry Strategies
| Scenario | Retry | Strategy |
|----------|-------|----------|
| Stock check fails | Yes | Immediate retry |
| SePay QR fails | Yes | Exponential backoff (3x) |
| Webhook fails | Yes | SePay auto-retry (5x, 5min) |
| DB timeout | Yes | Spring @Retryable (3x) |
| Payment timeout | No | Auto-cancel after 15min |

### 4. Performance Considerations

#### Database Optimization
- **Indexes**: 
  - `orders(order_code)` - UNIQUE
  - `orders(customer_id, status)` - Composite
  - `payments(payment_code)` - UNIQUE
  - `inventory_stock(product_id)`

- **Transaction Isolation**:
  - `READ_COMMITTED` for order creation
  - `SERIALIZABLE` for stock reservation (prevent race conditions)

- **Pessimistic Locking**:
  ```sql
  SELECT * FROM inventory_stock WHERE product_id = ? FOR UPDATE;
  ```

#### Concurrency Handling
- Pessimistic locking for stock reservation
- Transaction-based stock updates
- Idempotency checks for webhooks

### 5. Monitoring và Logging

#### Key Metrics
- Order creation rate (orders/minute)
- Payment success rate
- Stock check latency
- Webhook processing time

#### Log Events
- Order creation
- Stock reservation
- Payment webhook received
- Payment matched
- Error cases (insufficient stock, amount mismatch)

## Diagrams Created

### 1. COD Order Flow Sequence Diagram
- **Participants**: Customer, Frontend, API Gateway, OrderController, OrderService, InventoryService, CartService, Database
- **Phases**: 
  1. Customer Checkout
  2. Validation & Stock Check
  3. Reserve Stock & Create Order
  4. Return Response
- **Alternative Flows**: Form validation fails, address invalid, insufficient stock

### 2. Online Payment Order Creation Sequence Diagram
- **Participants**: Customer, Frontend, API Gateway, OrderController, OrderService, PaymentService, InventoryService, SePay API, Database
- **Phases**:
  1. Create Order with Online Payment
  2. Create Order & Payment Record
  3. Generate Payment QR Code
  4. Display QR Code
- **Alternative Flows**: Insufficient stock, SePay API error

### 3. SePay Webhook Processing Sequence Diagram
- **Participants**: Customer's Bank, SePay System, WebhookController, WebhookService, PaymentService, OrderService, AccountingService, Database, Frontend
- **Phases**:
  1. Customer Completes Transfer
  2. SePay Webhook
  3. Verify Webhook Signature
  4. Check Duplicate
  5. Match Payment
  6. Verify Amount
  7. Update Payment & Order
  8. Record Accounting Entry
  9. Frontend Polling
- **Alternative Flows**: Invalid signature, duplicate transaction, payment not found, amount mismatch

### 4. Payment Timeout Handling Sequence Diagram
- **Participants**: Spring Scheduler, PaymentService, OrderService, InventoryService, Database
- **Process**: Auto-cancel expired payments and release reserved stock

### 5. Validation Flow Diagram
- Decision tree showing all validation points from order request to order creation

## Technical Details

### Order Entity Fields
```java
- id, orderCode (unique)
- customer, status, paymentStatus
- paymentMethod, total, shippingFee
- shippingAddress, ghnOrderCode
- items (OneToMany)
- createdAt, confirmedAt, deliveredAt
```

### Payment Entity Fields
```java
- id, paymentCode (unique)
- orderId, amount, status
- transactionId, qrCode, qrDataURL
- expiredAt, paidAt
```

### Order Status Flow
```
COD: PENDING → CONFIRMED → READY_TO_PICK → READY_TO_SHIP → SHIPPING → DELIVERED
Online: PENDING_PAYMENT → CONFIRMED → ... (same as COD)
Cancelled: Any status → CANCELLED
```

## Business Rules Summary

### COD Orders
1. Created with status `PENDING`
2. Payment status `UNPAID`
3. Requires Sales staff confirmation
4. Stock reserved immediately

### Online Payment Orders
1. Created with status `PENDING_PAYMENT`
2. QR code expires in 15 minutes
3. Auto-confirmed when payment received
4. Auto-cancelled if payment timeout
5. Stock released on cancellation

### Stock Management
1. Reserve stock on order creation
2. Decrease stock on export completion
3. Release stock on order cancellation
4. Prevent overselling with pessimistic locking

### Payment Matching
1. Primary: Payment code in description
2. Verify: Amount must match exactly
3. Security: Webhook signature verification
4. Idempotency: Check transaction_id

## Quality Metrics

### Documentation Completeness
- ✅ All acceptance criteria covered (1.1-1.5, 8.1-8.5)
- ✅ Both COD and Online payment flows
- ✅ All validation points documented
- ✅ Error handling for all scenarios
- ✅ Performance considerations included
- ✅ Security measures explained
- ✅ Monitoring and logging guidelines

### Diagram Quality
- ✅ Mermaid syntax (compatible with academic reports)
- ✅ Clear participant labels
- ✅ Detailed notes and annotations
- ✅ Alternative flows (alt/else blocks)
- ✅ Activation/deactivation of components
- ✅ Database transaction boundaries
- ✅ HTTP status codes and error messages

### Technical Depth
- ✅ Database schema details
- ✅ Transaction isolation levels
- ✅ Concurrency handling
- ✅ Index strategy
- ✅ Retry mechanisms
- ✅ Logging patterns

## Integration with Other Tasks

### Related to Task 1 (Business Flow Analysis)
- Implements Flow 1: Order Management
- Covers standard and exception scenarios
- Aligns with business rules in design.md

### Related to Task 2 (Use Case Diagram)
- Implements UC-01: Browse and Purchase Products
- Implements UC-08: Process Online Payments
- Shows interaction between Customer and System

### Related to Task 3 (ERD Analysis)
- Uses entities: Order, OrderItem, Payment, InventoryStock
- Demonstrates relationships and foreign keys
- Shows transaction management

### Foundation for Future Tasks
- **Task 5**: Warehouse flow will reference stock reservation
- **Task 6**: GHN shipping will use READY_TO_SHIP status
- **Task 7**: Payment flow already documented here
- **Task 8**: Accounting integration shown in webhook processing

## Strengths

1. **Comprehensive Coverage**: Both payment methods fully documented
2. **Real-world Scenarios**: Based on actual implementation
3. **Security Focus**: Webhook verification, idempotency
4. **Error Handling**: Detailed error scenarios and recovery
5. **Performance**: Database optimization strategies
6. **Maintainability**: Clear logging and monitoring guidelines

## Trade-offs Documented

1. **Stock Reservation**: May lock inventory temporarily
   - Mitigation: Auto-cancel timeout for online payments

2. **Webhook Dependency**: Relies on external service
   - Mitigation: Polling, manual matching option

3. **Database Load**: Multiple queries for validation
   - Mitigation: Indexes, connection pooling, future caching

## Future Improvements Suggested

1. Async processing with message queue
2. Redis caching for product/address data
3. Rate limiting for order creation
4. Email/SMS notifications
5. Real-time analytics dashboard

## Conclusion

Task 4 has been completed successfully with comprehensive sequence diagrams covering:
- ✅ Customer checkout to order creation
- ✅ Both COD and Online payment methods
- ✅ Detailed step-by-step message flows
- ✅ Validation and error handling at all levels
- ✅ Performance and security considerations
- ✅ Monitoring and logging guidelines

The documentation is ready for inclusion in the graduation thesis report (báo cáo đồ án) and provides a solid foundation for understanding the order management system.

---

**Completed**: 2023-12-23  
**Document**: sequence-diagram-order-flow.md  
**Total Diagrams**: 5 comprehensive sequence diagrams  
**Total Pages**: ~15 pages of detailed documentation
