# Luồng Đặt Hàng và Thanh Toán Online - Mô Tả Chi Tiết

## Tổng Quan

Tài liệu này mô tả chi tiết luồng đặt hàng và thanh toán online qua SePay, từ khi thêm sản phẩm vào giỏ hàng đến khi thanh toán thành công. Ghi rõ từng bước gọi hàm, class, method cụ thể.

---

## PHẦN 0: THÊM SẢN PHẨM VÀO GIỎ HÀNG

### Bước 0.1: Khách Hàng Thêm Sản Phẩm Vào Giỏ

**Frontend**: User click nút "Thêm vào giỏ hàng" trên trang sản phẩm

**API Call**: `POST http://localhost:8080/api/cart/items`

**Headers**: 
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body**: `AddToCartRequest`
```json
{
  "productId": 123,
  "quantity": 2
}
```

#### Backend xử lý:

**Controller**: `CartController.java`
- **Method**: `addToCart(AddToCartRequest request, Authentication authentication)`
- **Annotation**: `@PostMapping("/items")`
- **Security**: `@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")`

**Code flow**:
```java
@PostMapping("/items")
public ApiResponse addToCart(@Valid @RequestBody AddToCartRequest request,
                             Authentication authentication) {
    Long customerId = getCustomerIdFromAuth(authentication);
    return cartService.addToCart(customerId, request);
}
```

**Helper method**: `getCustomerIdFromAuth(Authentication authentication)`
```java
private Long getCustomerIdFromAuth(Authentication authentication) {
    String email = authentication.getName(); // Lấy email từ JWT token
    Long customerId = cartService.getCustomerIdByEmail(email);
    return customerId;
}
```

**Service**: `CartServiceImpl.java`
- **Method**: `addToCart(Long customerId, AddToCartRequest request)`
- **Annotation**: `@Transactional`

**Code flow**:
```java
@Transactional
public ApiResponse addToCart(Long customerId, AddToCartRequest request) {
    // 1. Get or create cart
    Cart cart = cartRepository.findByCustomerId(customerId)
        .orElseGet(() -> {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
            Cart newCart = new Cart();
            newCart.setCustomer(customer);
            return cartRepository.save(newCart);
        });
    
    // 2. Get product
    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    
    // 3. Check stock
    if (product.getStockQuantity() < request.getQuantity()) {
        return ApiResponse.error("Sản phẩm không đủ số lượng");
    }
    
    // 4. Check if product already in cart
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getProduct().getId().equals(product.getId()))
        .findFirst();
    
    if (existingItem.isPresent()) {
        // Update quantity
        CartItem item = existingItem.get();
        item.setQuantity(item.getQuantity() + request.getQuantity());
        cartItemRepository.save(item);
    } else {
        // Add new item
        CartItem newItem = CartItem.builder()
            .cart(cart)
            .product(product)
            .quantity(request.getQuantity())
            .price(product.getPrice())
            .build();
        cart.getItems().add(newItem);
        cartItemRepository.save(newItem);
    }
    
    // 5. Save cart
    cartRepository.save(cart);
    
    return ApiResponse.success("Đã thêm vào giỏ hàng");
}
```

**SQL thực thi**:
```sql
-- Tìm cart
SELECT * FROM carts WHERE customer_id = ?;

-- Nếu chưa có cart, tạo mới
INSERT INTO carts (customer_id, created_at) VALUES (?, NOW());

-- Tìm product
SELECT * FROM products WHERE id = ?;

-- Kiểm tra item đã có trong cart chưa
SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?;

-- Nếu đã có, update quantity
UPDATE cart_items SET quantity = quantity + ? WHERE id = ?;

-- Nếu chưa có, insert mới
INSERT INTO cart_items (cart_id, product_id, quantity, price, created_at)
VALUES (?, ?, ?, ?, NOW());
```

**Response trả về Frontend**:
```json
{
  "success": true,
  "message": "Đã thêm vào giỏ hàng",
  "data": null
}
```

---

### Bước 0.2: Xem Giỏ Hàng

**API Call**: `GET http://localhost:8080/api/cart`

**Controller**: `CartController.java`
- **Method**: `getCart(Authentication authentication)`

**Service**: `CartServiceImpl.java`
- **Method**: `getCart(Long customerId)`

**Code flow**:
```java
public ApiResponse getCart(Long customerId) {
    Cart cart = cartRepository.findByCustomerId(customerId)
        .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));
    
    CartResponse response = toCartResponse(cart);
    return ApiResponse.success("Giỏ hàng", response);
}
```

**SQL**:
```sql
SELECT c.*, ci.*, p.* 
FROM carts c
LEFT JOIN cart_items ci ON c.id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.id
WHERE c.customer_id = ?;
```

**Response**:
```json
{
  "success": true,
  "message": "Giỏ hàng",
  "data": {
    "cartId": 1,
    "items": [
      {
        "itemId": 1,
        "productId": 123,
        "productName": "iPhone 15 Pro Max",
        "price": 30000000,
        "quantity": 2,
        "subtotal": 60000000
      }
    ],
    "total": 60000000
  }
}
```

---

## PHẦN 1: TẠO ĐỚN HÀNG

### Bước 1.1: Khách Hàng Checkout

**Frontend**: User click "Thanh toán" → Điền form:
- Địa chỉ giao hàng (address, ward, district, province)
- Ghi chú
- Chọn phương thức thanh toán: COD hoặc SEPAY (Online)

**API Call**: `POST http://localhost:8080/api/orders`

**Headers**: 
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body**: `CreateOrderRequest`
```json
{
  "address": "123 Nguyễn Trãi",
  "ward": "26734",
  "wardName": "Phường Thanh Xuân Trung",
  "district": "Thanh Xuân",
  "province": "Hà Nội",
  "note": "Giao giờ hành chính",
  "paymentMethod": "SEPAY",
  "shippingFee": 30000
}
```

---

### Bước 1.2: Backend Tạo Đơn Hàng

**Controller**: `OrderController.java`
- **Method**: `createOrder(CreateOrderRequest request, Authentication authentication)`
- **Annotation**: `@PostMapping`
- **Security**: `@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")`

**Code flow**:
```java
@PostMapping
public ApiResponse createOrder(@Valid @RequestBody CreateOrderRequest request,
                               Authentication authentication) {
    Long customerId = getCustomerIdFromAuth(authentication);
    return orderService.createOrderFromCart(customerId, request);
}
```

---

### Bước 1.3: OrderService Xử Lý Tạo Đơn

**Service**: `OrderServiceImpl.java`
- **Method**: `createOrderFromCart(Long customerId, CreateOrderRequest request)`
- **Annotation**: `@Transactional`

**Code flow chi tiết**:

#### 1.3.1. Get Customer
```java
Customer customer = customerRepository.findById(customerId)
    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
```

**SQL**:
```sql
SELECT * FROM customers WHERE id = ?;
```

#### 1.3.2. Get Cart
```java
Cart cart = cartRepository.findByCustomerId(customerId)
    .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

if (cart.getItems().isEmpty()) {
    return ApiResponse.error("Giỏ hàng trống");
}
```

**SQL**:
```sql
SELECT c.*, ci.*, p.* 
FROM carts c
LEFT JOIN cart_items ci ON c.id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.id
WHERE c.customer_id = ?;
```

#### 1.3.3. Validate Stock
```java
for (CartItem cartItem : cart.getItems()) {
    Product product = cartItem.getProduct();
    if (product.getStockQuantity() == null || 
        product.getStockQuantity() < cartItem.getQuantity()) {
        return ApiResponse.error("Sản phẩm " + product.getName() + " không đủ số lượng");
    }
}
```

#### 1.3.4. Calculate Totals
```java
Double subtotal = cart.getItems().stream()
    .mapToDouble(item -> item.getPrice() * item.getQuantity())
    .sum();
Double shippingFee = request.getShippingFee();
Double discount = 0.0;
Double total = subtotal + shippingFee - discount;
```

#### 1.3.5. Generate Order Code
```java
String orderCode = generateOrderCode();
// Format: ORD + YYYYMMDD + 4 số random
// Example: ORD20231223XXXX
```

**Helper method**: `generateOrderCode()`
```java
private String generateOrderCode() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    int random = new Random().nextInt(9999);
    String code = "ORD" + date + String.format("%04d", random);
    
    if (orderRepository.existsByOrderCode(code)) {
        return generateOrderCode(); // Retry nếu trùng
    }
    return code;
}
```

#### 1.3.6. Determine Initial Status
```java
OrderStatus initialStatus;
LocalDateTime confirmedTime = null;

if ("SEPAY".equals(request.getPaymentMethod())) {
    // Thanh toán online → PENDING_PAYMENT (chờ thanh toán)
    initialStatus = OrderStatus.PENDING_PAYMENT;
} else {
    // COD → CONFIRMED (tự động xác nhận)
    initialStatus = OrderStatus.CONFIRMED;
    confirmedTime = LocalDateTime.now();
}
```

#### 1.3.7. Create Order Entity
```java
String fullAddress = String.format("%s, %s, %s, %s",
    request.getAddress(), request.getWardName(), 
    request.getDistrict(), request.getProvince());

Order order = Order.builder()
    .orderCode(orderCode)
    .customer(customer)
    .shippingAddress(fullAddress)
    .province(request.getProvince())
    .district(request.getDistrict())
    .ward(request.getWard())
    .wardName(request.getWardName())
    .address(request.getAddress())
    .note(request.getNote())
    .subtotal(subtotal)
    .shippingFee(shippingFee)
    .discount(discount)
    .total(total)
    .status(initialStatus)
    .paymentStatus(PaymentStatus.UNPAID)
    .paymentMethod(request.getPaymentMethod())
    .confirmedAt(confirmedTime)
    .build();
```

#### 1.3.8. Create Order Items & Reserve Stock
```java
List<OrderItem> orderItems = new ArrayList<>();
for (CartItem cartItem : cart.getItems()) {
    Product product = cartItem.getProduct();
    
    // Reserve stock (giữ hàng)
    Long currentReserved = product.getReservedQuantity() != null 
        ? product.getReservedQuantity() : 0L;
    product.setReservedQuantity(currentReserved + cartItem.getQuantity());
    
    OrderItem orderItem = OrderItem.builder()
        .order(order)
        .product(product)
        .productName(product.getName())
        .price(cartItem.getPrice())
        .quantity(cartItem.getQuantity())
        .subtotal(cartItem.getPrice() * cartItem.getQuantity())
        .reserved(true)  // Đã giữ hàng
        .exported(false) // Chưa xuất kho
        .build();
    
    orderItems.add(orderItem);
}
order.setItems(orderItems);
```

**SQL**:
```sql
-- Update reserved quantity
UPDATE products 
SET reserved_quantity = reserved_quantity + ?
WHERE id = ?;
```

#### 1.3.9. Save Order
```java
Order savedOrder = orderRepository.save(order);
```

**SQL**:
```sql
-- Insert order
INSERT INTO orders (
    order_code, customer_id, shipping_address, province, district, ward, ward_name,
    address, note, subtotal, shipping_fee, discount, total, 
    status, payment_status, payment_method, confirmed_at, created_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW());

-- Insert order items
INSERT INTO order_items (
    order_id, product_id, product_name, price, quantity, subtotal, 
    reserved, exported, created_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW());
```

#### 1.3.10. Clear Cart
```java
cart.clearItems();
cartRepository.save(cart);
```

**SQL**:
```sql
DELETE FROM cart_items WHERE cart_id = ?;
```

#### 1.3.11. Return Response
```java
OrderResponse response = toOrderResponse(savedOrder);
return ApiResponse.success("Đặt hàng thành công", response);
```

**Response trả về Frontend**:
```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "orderId": 123,
    "orderCode": "ORD20231223XXXX",
    "status": "PENDING_PAYMENT",
    "paymentStatus": "UNPAID",
    "paymentMethod": "SEPAY",
    "total": 60030000,
    "items": [...],
    "shippingAddress": "123 Nguyễn Trãi, Phường Thanh Xuân Trung, Thanh Xuân, Hà Nội"
  }
}
```

---

## PHẦN 2: TẠO PAYMENT VÀ HIỂN THỊ QR CODE

### Bước 2.1: Frontend Nhận Response từ Create Order

**Frontend**: `src/frontend/app/checkout/page.tsx`

**Sau khi gọi API tạo đơn hàng thành công**, backend trả về response:

```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "orderId": 123,
    "orderCode": "ORD20231223XXXX",
    "status": "PENDING_PAYMENT",
    "paymentStatus": "UNPAID",
    "paymentMethod": "SEPAY",
    "total": 60030000,
    ...
  }
}
```

**Frontend xử lý**:
```typescript
const response = await orderApi.create(orderData)

if (response.success && response.data) {
  const orderId = response.data.orderId || response.data.id
  const orderCode = response.data.orderCode
  
  // Clear cart
  await cartApi.clearCart()
  window.dispatchEvent(new Event('cartUpdated'))
  
  // Kiểm tra payment method
  if (form.paymentMethod === 'SEPAY') {
    // Thanh toán online → Tạo payment
    // Xem bước 2.2
  } else {
    // COD → Redirect đến success page
    router.push(`/orders/success?orderId=${orderId}`)
  }
}
```

---

### Bước 2.2: Frontend Gọi API Tạo Payment

**Chỉ khi paymentMethod === 'SEPAY'**, frontend mới gọi API tạo payment:

**API Call**: `POST http://localhost:8080/api/payment/create`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body**: `CreatePaymentRequest`
```json
{
  "orderId": 123,
  "amount": 60030000
}
```

**Frontend code**:
```typescript
if (form.paymentMethod === 'SEPAY') {
  toast.loading('Đang tạo thanh toán...')
  
  const paymentResponse = await fetch('http://localhost:8080/api/payment/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    body: JSON.stringify({
      orderId: orderId,
      amount: calculateTotal()
    })
  })
  
  const paymentResult = await paymentResponse.json()
  
  if (paymentResult.success) {
    toast.dismiss()
    toast.success('Chuyển đến trang thanh toán...')
    // Redirect đến trang payment
    router.push(`/payment/${orderCode}`)
  } else {
    toast.error(paymentResult.message || 'Không thể tạo thanh toán')
    router.push(`/orders/${orderCode}`)
  }
}
```

---

### Bước 2.3: Backend Tạo Payment và Generate QR Code

**Controller**: `PaymentController.java`
- **Method**: `createPayment(CreatePaymentRequest request, Authentication authentication)`
- **Annotation**: `@PostMapping("/create")`
- **Security**: `@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")`

**Code flow**:
```java
@PostMapping("/create")
public ApiResponse createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                 Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    return paymentService.createPayment(request, userId);
}
```

---

### Bước 2.4: PaymentService Xử Lý Tạo Payment

**Service**: `PaymentServiceImpl.java`
- **Method**: `createPayment(CreatePaymentRequest request, Long userId)`
- **Annotation**: `@Transactional`

**Code flow chi tiết**:

#### 2.4.1. Validate Order
```java
Order order = orderRepository.findById(request.getOrderId())
    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

User user = userRepository.findById(userId)
    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

// Verify ownership
if (!order.getCustomer().getUser().getId().equals(userId)) {
    return ApiResponse.error("Bạn không có quyền thanh toán đơn hàng này");
}
```

**SQL**:
```sql
SELECT * FROM orders WHERE id = ?;
SELECT * FROM users WHERE id = ?;
```

#### 2.4.2. Check if Payment Already Exists
```java
if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
    return ApiResponse.error("Đơn hàng này đã có thanh toán");
}
```

**SQL**:
```sql
SELECT * FROM payments WHERE order_id = ?;
```

#### 2.4.3. Validate Amount
```java
if (!request.getAmount().equals(order.getTotal())) {
    return ApiResponse.error("Số tiền thanh toán không khớp với đơn hàng");
}
```

#### 2.4.4. Generate Payment Code
```java
String paymentCode = generatePaymentCode();
// Format: PAY + YYYYMMDD + 4 số random
// Example: PAY20231223XXXX
```

**Helper method**:
```java
private String generatePaymentCode() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    int random = new Random().nextInt(9999);
    String code = "PAY" + date + String.format("%04d", random);
    
    if (paymentRepository.existsByPaymentCode(code)) {
        return generatePaymentCode(); // Retry
    }
    return code;
}
```

#### 2.4.5. Get Bank Account
```java
BankAccount bankAccount = bankAccountRepository.findByIsDefaultTrue()
    .orElse(null);

// Fallback to config if no default account
String bankCode = bankAccount != null ? bankAccount.getBankCode() : sepayBankCode;
String accountNumber = bankAccount != null ? bankAccount.getAccountNumber() : sepayAccountNumber;
String accountName = bankAccount != null ? bankAccount.getAccountName() : sepayAccountName;
```

**SQL**:
```sql
SELECT * FROM bank_accounts WHERE is_default = true;
```

**Config fallback** (từ `application.properties`):
```properties
sepay.bank.code=MBBank
sepay.bank.account.number=3333315012003
sepay.bank.account.name=CONG TY TNHH TECHMART
```

#### 2.4.6. Generate QR Code URL
```java
String qrCodeUrl = generateSepayQrCode(paymentCode, request.getAmount(), 
                                       bankCode, accountNumber, accountName);
```

**Helper method**: `generateSepayQrCode()`
```java
private String generateSepayQrCode(String content, Double amount, 
                                   String bankCode, String accountNumber, String accountName) {
    long amountInVnd = Math.round(amount * amountMultiplier);
    
    // VietQR format - template: qr_only (chỉ có mã QR, không có text)
    return String.format(
        "https://img.vietqr.io/image/%s-%s-qr_only.jpg?amount=%d&addInfo=%s&accountName=%s",
        bankCode,           // MBBank
        accountNumber,      // 3333315012003
        amountInVnd,        // 60030000
        content,            // PAY20231223XXXX
        accountName.replace(" ", "%20")  // CONG%20TY%20TNHH%20TECHMART
    );
}
```

**QR Code URL example**:
```
https://img.vietqr.io/image/MBBank-3333315012003-qr_only.jpg?amount=60030000&addInfo=PAY20231223XXXX&accountName=CONG%20TY%20TNHH%20TECHMART
```

**Giải thích VietQR API**:
- **Service**: VietQR (https://vietqr.io) - Free API generate QR code
- **Format**: `https://img.vietqr.io/image/{bankCode}-{accountNumber}-{template}.jpg`
- **Parameters**:
  - `amount`: Số tiền (VND)
  - `addInfo`: Nội dung chuyển khoản (payment code)
  - `accountName`: Tên tài khoản
- **Template**: `qr_only` - Chỉ hiển thị mã QR, không có thông tin ngân hàng

#### 2.4.7. Create Payment Entity
```java
Payment payment = Payment.builder()
    .paymentCode(paymentCode)
    .order(order)
    .user(user)
    .amount(request.getAmount())
    .method(PaymentMethod.SEPAY)
    .status(PaymentStatus.PENDING)
    .sepayBankCode(bankCode)
    .sepayAccountNumber(accountNumber)
    .sepayAccountName(accountName)
    .sepayContent(paymentCode)
    .sepayQrCode(qrCodeUrl)
    .build();

Payment savedPayment = paymentRepository.save(payment);
```

**SQL**:
```sql
INSERT INTO payments (
    payment_code, order_id, user_id, amount, method, status,
    sepay_bank_code, sepay_account_number, sepay_account_name,
    sepay_content, sepay_qr_code, created_at, expired_at
) VALUES (
    'PAY20231223XXXX', 123, 456, 60030000, 'SEPAY', 'PENDING',
    'MBBank', '3333315012003', 'CONG TY TNHH TECHMART',
    'PAY20231223XXXX', 'https://img.vietqr.io/image/...',
    NOW(), NOW() + INTERVAL 15 MINUTE
);
```

**Note**: `expired_at` được set tự động trong `@PrePersist`:
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    expiredAt = createdAt.plusMinutes(15); // Hết hạn sau 15 phút
}
```

#### 2.4.8. Update Order
```java
order.setPaymentId(savedPayment.getId());
order.setPaymentStatus(com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PENDING);
orderRepository.save(order);
```

**SQL**:
```sql
UPDATE orders 
SET payment_id = ?, payment_status = 'PENDING'
WHERE id = ?;
```

#### 2.4.9. Build Response
```java
PaymentResponse response = PaymentResponse.builder()
    .paymentId(savedPayment.getId())
    .paymentCode(savedPayment.getPaymentCode())
    .amount(savedPayment.getAmount())
    .status(savedPayment.getStatus().name())
    .bankCode(savedPayment.getSepayBankCode())
    .accountNumber(savedPayment.getSepayAccountNumber())
    .accountName(savedPayment.getSepayAccountName())
    .content(savedPayment.getSepayContent())
    .qrCodeUrl(savedPayment.getSepayQrCode())
    .expiredAt(savedPayment.getExpiredAt().toString())
    .message("Vui lòng quét mã QR hoặc chuyển khoản với nội dung: " + paymentCode)
    .build();

return ApiResponse.success("Tạo thanh toán thành công", response);
```

---

### Bước 2.5: Backend Trả Response về Frontend

**Response trả về**:
```json
{
  "success": true,
  "message": "Tạo thanh toán thành công",
  "data": {
    "paymentId": 456,
    "paymentCode": "PAY20231223XXXX",
    "amount": 60030000,
    "status": "PENDING",
    "bankCode": "MBBank",
    "accountNumber": "3333315012003",
    "accountName": "CONG TY TNHH TECHMART",
    "content": "PAY20231223XXXX",
    "qrCodeUrl": "https://img.vietqr.io/image/MBBank-3333315012003-qr_only.jpg?amount=60030000&addInfo=PAY20231223XXXX&accountName=CONG%20TY%20TNHH%20TECHMART",
    "expiredAt": "2023-12-23T15:30:00",
    "message": "Vui lòng quét mã QR hoặc chuyển khoản với nội dung: PAY20231223XXXX"
  }
}
```

---

### Bước 2.6: Frontend Redirect đến Trang Thanh Toán

**Frontend nhận response**:
```typescript
const paymentResult = await paymentResponse.json()

if (paymentResult.success) {
  toast.dismiss()
  toast.success('Chuyển đến trang thanh toán...')
  // Redirect đến trang payment với orderCode
  router.push(`/payment/${orderCode}`)
}
```

**URL**: `/payment/ORD20231223XXXX`

**Trang này sẽ**:
1. Load lại payment info từ API (để có QR code URL)
2. Hiển thị QR code: `<img src={payment.qrCodeUrl} />`
3. Hiển thị thông tin ngân hàng
4. Start polling để check payment status

---

## TÓM TẮT FLOW TẠO PAYMENT VÀ HIỂN THỊ QR

### Backend Flow:
1. **OrderController.createOrder()** → Tạo order với status = PENDING_PAYMENT
2. **Frontend nhận orderCode** → Gọi tiếp **PaymentController.createPayment()**
3. **PaymentService.createPayment()**:
   - Validate order, user, amount
   - Generate payment code (PAY20231223XXXX)
   - Get bank account info (từ DB hoặc config)
   - **Generate QR code URL** bằng VietQR API
   - Save payment entity với qrCodeUrl
   - Update order.paymentId
   - Return PaymentResponse (có qrCodeUrl)
4. **Frontend redirect** → `/payment/{orderCode}`
5. **PaymentPage load** → Gọi `GET /api/payment/order/{orderId}`
6. **Backend trả payment info** (có qrCodeUrl)
7. **Frontend render** → `<img src={payment.qrCodeUrl} />`

### Key Points:
- **QR Code không lưu file**: Chỉ lưu URL, VietQR API generate real-time
- **Payment tạo sau Order**: Order tạo trước, payment tạo sau (chỉ khi SEPAY)
- **QR Code URL format**: `https://img.vietqr.io/image/{bank}-{account}-qr_only.jpg?amount={amount}&addInfo={paymentCode}&accountName={name}`
- **Expiration**: Payment hết hạn sau 15 phút (set trong @PrePersist)

---

## PHẦN 3: KHÁCH HÀNG VÀO TRANG THANH TOÁN

## PHẦN 3: KHÁCH HÀNG VÀO TRANG THANH TOÁN

### Bước 3.1: Frontend Load Payment Page

**File**: `src/frontend/app/payment/[orderCode]/page.tsx`

**Component**: `PaymentPage`

**Khi user truy cập**: `/payment/ORD20231223XXXX`

#### 3.1.1. useEffect() - Kiểm tra authentication

```typescript
useEffect(() => {
  const token = localStorage.getItem('auth_token')
  if (!isAuthenticated && !token) {
    toast.error('Vui lòng đăng nhập')
    router.push('/login')
    return
  }
  loadPaymentInfo()
}, [isAuthenticated, params.orderCode])
```

**Giải thích**: 
- Check xem user đã login chưa
- Nếu chưa → redirect về `/login`
- Nếu rồi → gọi `loadPaymentInfo()`

---

### Bước 3.2: Load Order Information

**Function**: `loadPaymentInfo()` trong PaymentPage

**API Call 1**: `GET http://localhost:8080/api/orders/code/{orderCode}`

**Headers**: 
```
Authorization: Bearer {token từ localStorage}
```

#### Backend xử lý:

**Controller**: `OrderController.java`
- **Method**: `getOrderByCode(String orderCode)`
- **Annotation**: `@GetMapping("/code/{orderCode}")`
- **Security**: `@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")`

**Code flow**:
```java
public ApiResponse getOrderByCode(@PathVariable String orderCode) {
    return orderService.getOrderByCode(orderCode);
}
```

**Service**: `OrderServiceImpl.java`
- **Method**: `getOrderByCode(String orderCode)`

**Code flow**:
```java
public ApiResponse getOrderByCode(String orderCode) {
    Order order = orderRepository.findByOrderCode(orderCode)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    
    OrderResponse response = toOrderResponse(order);
    return ApiResponse.success("Thông tin đơn hàng", response);
}
```

**Repository**: `OrderRepository.java`
- **Method**: `findByOrderCode(String orderCode)`
- **Query**: `SELECT * FROM orders WHERE order_code = ?`

**Response trả về Frontend**:
```json
{
  "success": true,
  "message": "Thông tin đơn hàng",
  "data": {
    "orderId": 123,
    "orderCode": "ORD20231223XXXX",
    "status": "PENDING_PAYMENT",
    "paymentStatus": "PENDING",
    "total": 500000,
    ...
  }
}
```

**Frontend nhận response**:
```typescript
const orderData = await orderResponse.json()
setOrder(orderData.data)
```

---

### Bước 3.3: Load Payment Information

**API Call 2**: `GET http://localhost:8080/api/payment/order/{orderId}`

**Headers**: 
```
Authorization: Bearer {token}
```

#### Backend xử lý:

**Controller**: `PaymentController.java`
- **Method**: `getPaymentByOrderId(Long orderId)`
- **Annotation**: `@GetMapping("/order/{orderId}")`
- **Security**: `@PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")`

**Code flow**:
```java
@GetMapping("/order/{orderId}")
public ApiResponse getPaymentByOrderId(@PathVariable Long orderId) {
    return paymentService.getPaymentByOrderId(orderId);
}
```

**Service**: `PaymentServiceImpl.java`
- **Method**: `getPaymentByOrderId(Long orderId)`

**Code flow**:
```java
public ApiResponse getPaymentByOrderId(Long orderId) {
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));
    
    PaymentResponse response = toPaymentResponse(payment);
    return ApiResponse.success("Thông tin thanh toán", response);
}
```

**Repository**: `PaymentRepository.java`
- **Method**: `findByOrderId(Long orderId)`
- **Query**: `SELECT * FROM payments WHERE order_id = ?`

**Entity**: `Payment.java`
- **Fields**:
  - `id`: Long
  - `paymentCode`: String (PAY20231223XXXX)
  - `order`: Order (OneToOne)
  - `user`: User (ManyToOne)
  - `amount`: Double
  - `method`: PaymentMethod (SEPAY)
  - `status`: PaymentStatus (PENDING/SUCCESS/EXPIRED/FAILED)
  - `sepayBankCode`: String (VCB, MBBank, ...)
  - `sepayAccountNumber`: String
  - `sepayAccountName`: String
  - `sepayContent`: String (PAY20231223XXXX)
  - `sepayQrCode`: String (URL)
  - `sepayTransactionId`: String
  - `sepayResponse`: String (TEXT)
  - `createdAt`: LocalDateTime
  - `paidAt`: LocalDateTime
  - `expiredAt`: LocalDateTime (createdAt + 15 phút)
  - `failureReason`: String

**Helper method**: `toPaymentResponse(Payment payment)`

**Code flow**:
```java
private PaymentResponse toPaymentResponse(Payment payment) {
    return PaymentResponse.builder()
        .paymentId(payment.getId())
        .paymentCode(payment.getPaymentCode())
        .amount(payment.getAmount())
        .status(payment.getStatus().name())
        .bankCode(payment.getSepayBankCode())
        .accountNumber(payment.getSepayAccountNumber())
        .accountName(payment.getSepayAccountName())
        .content(payment.getSepayContent())
        .qrCodeUrl(payment.getSepayQrCode())
        .expiredAt(payment.getExpiredAt().toString())
        .build();
}
```

**DTO**: `PaymentResponse.java`
```java
public class PaymentResponse {
    private Long paymentId;
    private String paymentCode;
    private Double amount;
    private String status;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String content;
    private String qrCodeUrl;
    private String expiredAt;
    private String message;
}
```

**Response trả về Frontend**:
```json
{
  "success": true,
  "message": "Thông tin thanh toán",
  "data": {
    "paymentId": 456,
    "paymentCode": "PAY20231223XXXX",
    "amount": 500000,
    "status": "PENDING",
    "bankCode": "MBBank",
    "accountNumber": "3333315012003",
    "accountName": "CONG TY TNHH TECHMART",
    "content": "PAY20231223XXXX",
    "qrCodeUrl": "https://img.vietqr.io/image/MBBank-3333315012003-qr_only.jpg?amount=500000&addInfo=PAY20231223XXXX&accountName=CONG%20TY%20TNHH%20TECHMART",
    "expiredAt": "2023-12-23T15:30:00"
  }
}
```

---

### Bước 3.4: Frontend Hiển Thị Trang Thanh Toán

**Frontend nhận response**:
```typescript
const paymentData = await paymentResponse.json()
setPayment(paymentData.data)

// Tính thời gian còn lại
const expiredTime = new Date(paymentData.data.expiredAt).getTime()
const now = Date.now()
const secondsLeft = Math.max(0, Math.floor((expiredTime - now) / 1000))
setTimeLeft(secondsLeft)
```

**Start Polling**:
```typescript
useEffect(() => {
  if (payment && !pollingInterval.current) {
    startPolling()
  }
}, [payment])

const startPolling = () => {
  pollingInterval.current = setInterval(async () => {
    await checkPaymentStatus()
  }, 15000) // 15 giây
}
```

**Render UI**:
- QR Code: `<img src={payment.qrCodeUrl} />`
- Bank info: bankCode, accountNumber, accountName
- Amount: formatPrice(payment.amount)
- Content: payment.content (với nút copy)
- Countdown timer: formatTime(timeLeft)
- Nút "Kiểm tra thanh toán"
- Nút "Hủy đơn hàng"

---

## PHẦN 4: KHÁCH HÀNG CHUYỂN KHOẢN

### Bước 4.1: Khách Hàng Thực Hiện Chuyển Khoản

**Khách hàng**:
1. Mở app ngân hàng (MBBank, VCB, ...)
2. Quét QR code HOẶC nhập thủ công:
   - Số tài khoản: `3333315012003`
   - Số tiền: `500000`
   - Nội dung: `PAY20231223XXXX`
3. Xác nhận chuyển khoản

**Ngân hàng**:
- Xử lý giao dịch
- Gửi thông báo cho SePay

**SePay**:
- Nhận thông báo từ ngân hàng
- Gọi webhook của hệ thống

---

## PHẦN 5: XỬ LÝ WEBHOOK TỪ SEPAY

### Bước 5.1: SePay Gọi Webhook

**API Call**: `POST http://localhost:8080/api/payment/sepay/webhook`

**Headers**: 
```
Content-Type: application/json
```

**Body**: `SepayWebhookRequest`
```json
{
  "id": "TXN123456789",
  "gateway": "MBBank",
  "accountNumber": "3333315012003",
  "transferAmount": 500000,
  "content": "PAY20231223XXXX FT2533",
  "transactionDate": "2023-12-23 14:25:30",
  "status": "SUCCESS",
  "signature": "abc123xyz..."
}
```

#### Backend xử lý:

**Controller**: `PaymentController.java`
- **Method**: `handleSepayWebhook(SepayWebhookRequest request)`
- **Annotation**: `@PostMapping("/sepay/webhook")`
- **Security**: Public (không cần auth)

**Code flow**:
```java
@PostMapping("/sepay/webhook")
public ApiResponse handleSepayWebhook(@RequestBody SepayWebhookRequest request) {
    log.info("Received SePay webhook for payment: {}", request.getContent());
    return paymentService.handleSepayWebhook(request);
}
```

---

### Bước 5.2: PaymentService Xử Lý Webhook

**Service**: `PaymentServiceImpl.java`
- **Method**: `handleSepayWebhook(SepayWebhookRequest request)`
- **Annotation**: `@Transactional`

**Code flow chi tiết**:

#### 5.2.1. Validate Content
```java
String content = request.getContent(); // "PAY20231223XXXX FT2533"
if (content == null || !content.contains("PAY")) {
    log.warn("Webhook rejected - content doesn't contain payment code");
    return ApiResponse.error("Nội dung không chứa mã thanh toán");
}
```

#### 5.2.2. Extract Payment Code
```java
String paymentCode = extractPaymentCode(content);
// extractPaymentCode() sẽ tách "PAY20231223XXXX" từ "PAY20231223XXXX FT2533"
log.info("Extracted payment code: {}", paymentCode);
```

**Helper method**: `extractPaymentCode(String content)`
```java
private String extractPaymentCode(String content) {
    int index = content.indexOf("PAY");
    if (index != -1) {
        int endIndex = Math.min(index + 15, content.length());
        String extracted = content.substring(index, endIndex).split("\\s+")[0];
        return extracted;
    }
    return content.trim();
}
```

#### 5.2.3. Find Payment
```java
Payment payment = paymentRepository.findByPaymentCode(paymentCode)
    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));
```

**Repository**: `PaymentRepository.java`
- **Method**: `findByPaymentCode(String paymentCode)`
- **Query**: `SELECT * FROM payments WHERE payment_code = ?`

#### 5.2.4. Get Bank Account for Signature Verification
```java
BankAccount bankAccount = bankAccountRepository.findByIsDefaultTrue()
    .orElse(null);
```

**Repository**: `BankAccountRepository.java`
- **Method**: `findByIsDefaultTrue()`
- **Query**: `SELECT * FROM bank_accounts WHERE is_default = true`

#### 5.2.5. Verify Signature
```java
if (bankAccount != null && bankAccount.getSepayApiToken() != null) {
    if (!verifySignature(request, bankAccount.getSepayApiToken())) {
        log.error("Invalid signature from SePay webhook");
        return ApiResponse.error("Chữ ký không hợp lệ");
    }
}
```

#### 5.2.6. Check Payment Status
```java
if (payment.getStatus() == PaymentStatus.SUCCESS) {
    log.warn("Payment already processed: {}", payment.getPaymentCode());
    return ApiResponse.success("Thanh toán đã được xử lý");
}
```

#### 5.2.7. Validate Amount
```java
if (!payment.getAmount().equals(request.getAmount())) {
    log.error("Amount mismatch. Expected: {}, Received: {}", 
              payment.getAmount(), request.getAmount());
    return ApiResponse.error("Số tiền không khớp");
}
```

#### 5.2.8. Check Expiration
```java
if (LocalDateTime.now().isAfter(payment.getExpiredAt())) {
    payment.setStatus(PaymentStatus.EXPIRED);
    payment.setFailureReason("Thanh toán đã hết hạn");
    paymentRepository.save(payment);
    return ApiResponse.error("Thanh toán đã hết hạn");
}
```

---

### Bước 5.3: Update Payment (Transaction)

**Database Transaction**: `@Transactional` đảm bảo atomicity

#### 5.3.1. Update Payment Entity
```java
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
    sepay_response = '{"id":"TXN123456789",...}'
WHERE id = 456;
```

#### 5.3.2. Get Order from Payment
```java
Order order = payment.getOrder();
OrderStatus oldStatus = order.getStatus();
```

#### 5.3.3. Update Order Entity
```java
order.setPaymentStatus(com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PAID);
order.setStatus(com.doan.WEB_TMDT.module.order.entity.OrderStatus.CONFIRMED);
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

---

### Bước 5.4: Publish Event cho Accounting Module

**Service**: `PaymentServiceImpl.java`

**Code**:
```java
try {
    OrderStatusChangedEvent event = new OrderStatusChangedEvent(
        this, order, oldStatus, order.getStatus()
    );
    eventPublisher.publishEvent(event);
    log.info("Published OrderStatusChangedEvent for order: {}", 
             order.getOrderCode());
} catch (Exception e) {
    log.error("Failed to publish event", e);
    // Không fail payment process nếu event publishing lỗi
}
```

**Event**: `OrderStatusChangedEvent.java`
```java
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    
    public OrderStatusChangedEvent(Object source, Order order, 
                                   OrderStatus oldStatus, OrderStatus newStatus) {
        super(source);
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
```

**Listener**: `AccountingEventListener.java`
- **Method**: `handleOrderStatusChanged(OrderStatusChangedEvent event)`
- **Annotation**: `@EventListener`, `@Async`

**Code flow**:
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

**Accounting Service**: `AccountingServiceImpl.java`
- **Method**: `recordOrderRevenue(Order order)`

**Code flow**:
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
INSERT INTO financial_transactions 
(order_id, type, category, amount, transaction_date, description, created_at)
VALUES 
(123, 'REVENUE', 'ONLINE_PAYMENT', 500000, '2023-12-23', 
 'Doanh thu từ đơn hàng ORD20231223XXXX', '2023-12-23 14:25:30');
```

---

### Bước 5.5: Return Response cho SePay

**Service**: `PaymentServiceImpl.java`

**Code**:
```java
log.info("Payment processed successfully: {}", payment.getPaymentCode());
return ApiResponse.success("Xử lý thanh toán thành công");
```

**Controller**: `PaymentController.java`

**Response trả về SePay**:
```json
{
  "success": true,
  "message": "Xử lý thanh toán thành công",
  "data": null
}
```

**HTTP Status**: `200 OK`

---

## PHẦN 6: FRONTEND POLLING PHÁT HIỆN THÀNH CÔNG

### Bước 6.1: Frontend Polling Check Status

**Frontend**: `PaymentPage` component

**Polling interval trigger** (mỗi 15 giây):
```typescript
const checkPaymentStatus = async () => {
  if (checking || !payment) return
  
  setChecking(true)
  try {
    const response = await fetch(
      `http://localhost:8080/api/payment/${payment.paymentCode}/status`
    )
    
    if (response.ok) {
      const data = await response.json()
      
      if (data.success && data.data) {
        setPayment(data.data)
        
        if (data.data.status === 'SUCCESS') {
          handlePaymentSuccess()
        }
      }
    }
  } finally {
    setChecking(false)
  }
}
```

**API Call**: `GET http://localhost:8080/api/payment/{paymentCode}/status`

**Security**: Public (không cần auth)

---

### Bước 6.2: Backend Check Payment Status

**Controller**: `PaymentController.java`
- **Method**: `checkPaymentStatus(String paymentCode)`
- **Annotation**: `@GetMapping("/{paymentCode}/status")`

**Code flow**:
```java
@GetMapping("/{paymentCode}/status")
public ApiResponse checkPaymentStatus(@PathVariable String paymentCode) {
    ApiResponse response = paymentService.checkPaymentStatus(paymentCode);
    
    // Auto-trigger webhook nếu vẫn PENDING (workaround cho test)
    if (response.isSuccess() && response.getData() != null) {
        Map<String, Object> paymentMap = (Map<String, Object>) response.getData();
        String status = (String) paymentMap.get("status");
        
        if ("PENDING".equals(status)) {
            log.info("Payment still PENDING, auto-triggering webhook...");
            testWebhook(paymentCode);
            return paymentService.checkPaymentStatus(paymentCode);
        }
    }
    
    return response;
}
```

**Service**: `PaymentServiceImpl.java`
- **Method**: `checkPaymentStatus(String paymentCode)`

**Code flow**:
```java
public ApiResponse checkPaymentStatus(String paymentCode) {
    Payment payment = paymentRepository.findByPaymentCode(paymentCode)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));
    
    // Check if expired
    if (payment.getStatus() == PaymentStatus.PENDING && 
        LocalDateTime.now().isAfter(payment.getExpiredAt())) {
        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);
    }
    
    PaymentResponse response = toPaymentResponse(payment);
    return ApiResponse.success("Trạng thái thanh toán", response);
}
```

**Response trả về Frontend**:
```json
{
  "success": true,
  "message": "Trạng thái thanh toán",
  "data": {
    "paymentId": 456,
    "paymentCode": "PAY20231223XXXX",
    "status": "SUCCESS",
    ...
  }
}
```

---

### Bước 6.3: Frontend Xử Lý Success

**Frontend**: `PaymentPage` component

**Code**:
```typescript
if (data.data.status === 'SUCCESS') {
  handlePaymentSuccess()
}

const handlePaymentSuccess = () => {
  // Stop polling
  if (pollingInterval.current) {
    clearInterval(pollingInterval.current)
  }
  
  // Show success message
  toast.success('Thanh toán thành công!')
  
  // Redirect to order detail
  setTimeout(() => {
    router.push(`/orders/${params.orderCode}?success=true`)
  }, 1500)
}
```

**User thấy**:
1. Toast notification: "✅ Thanh toán thành công!"
2. Redirect về trang chi tiết đơn hàng
3. Đơn hàng có status = "CONFIRMED"
4. Payment status = "PAID"

---

## TÓM TẮT LUỒNG

### Frontend:
1. `PaymentPage.tsx` → `useEffect()` → `loadPaymentInfo()`
2. `fetch()` → `GET /api/orders/code/{orderCode}`
3. `fetch()` → `GET /api/payment/order/{orderId}`
4. `startPolling()` → `setInterval()` → `checkPaymentStatus()` (mỗi 15s)
5. `fetch()` → `GET /api/payment/{paymentCode}/status`
6. Detect `status === 'SUCCESS'` → `handlePaymentSuccess()` → `router.push()`

### Backend Controllers:
1. `OrderController.getOrderByCode()` → `OrderService.getOrderByCode()`
2. `PaymentController.getPaymentByOrderId()` → `PaymentService.getPaymentByOrderId()`
3. `PaymentController.handleSepayWebhook()` → `PaymentService.handleSepayWebhook()`
4. `PaymentController.checkPaymentStatus()` → `PaymentService.checkPaymentStatus()`

### Backend Services:
1. `OrderServiceImpl.getOrderByCode()` → `OrderRepository.findByOrderCode()`
2. `PaymentServiceImpl.getPaymentByOrderId()` → `PaymentRepository.findByOrderId()`
3. `PaymentServiceImpl.handleSepayWebhook()`:
   - `extractPaymentCode()`
   - `PaymentRepository.findByPaymentCode()`
   - `BankAccountRepository.findByIsDefaultTrue()`
   - `verifySignature()`
   - `PaymentRepository.save()` (update payment)
   - `OrderRepository.save()` (update order)
   - `ApplicationEventPublisher.publishEvent()`
4. `PaymentServiceImpl.checkPaymentStatus()` → `PaymentRepository.findByPaymentCode()`

### Backend Repositories:
1. `OrderRepository.findByOrderCode()` → SQL: `SELECT * FROM orders WHERE order_code = ?`
2. `PaymentRepository.findByOrderId()` → SQL: `SELECT * FROM payments WHERE order_id = ?`
3. `PaymentRepository.findByPaymentCode()` → SQL: `SELECT * FROM payments WHERE payment_code = ?`
4. `BankAccountRepository.findByIsDefaultTrue()` → SQL: `SELECT * FROM bank_accounts WHERE is_default = true`
5. `PaymentRepository.save()` → SQL: `UPDATE payments SET ...`
6. `OrderRepository.save()` → SQL: `UPDATE orders SET ...`

### Backend Events:
1. `PaymentServiceImpl` → `publishEvent(OrderStatusChangedEvent)`
2. `AccountingEventListener.handleOrderStatusChanged()` → `AccountingService.recordOrderRevenue()`
3. `FinancialTransactionRepository.save()` → SQL: `INSERT INTO financial_transactions ...`

### Entities:
1. `Order` (orders table)
2. `Payment` (payments table)
3. `BankAccount` (bank_accounts table)
4. `FinancialTransaction` (financial_transactions table)

### DTOs:
1. `CreatePaymentRequest` (input)
2. `PaymentResponse` (output)
3. `SepayWebhookRequest` (webhook input)
4. `OrderResponse` (output)

---

**Document Version**: 1.0  
**Last Updated**: 2024-12-25  
**Author**: System Analysis Team
