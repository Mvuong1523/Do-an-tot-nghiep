# Phân Tích Tích Hợp Với External Services

## Tổng Quan

Hệ thống tích hợp với 3 dịch vụ bên ngoài chính:
1. **GHN (Giao Hàng Nhanh)** - Dịch vụ vận chuyển
2. **SePay** - Dịch vụ thanh toán online qua QR code
3. **Cloudinary** - Dịch vụ lưu trữ và quản lý hình ảnh

Mỗi dịch vụ có các yêu cầu riêng về authentication, error handling, và security.

---

## 1. GHN (Giao Hàng Nhanh) Integration

### 1.1. Thông Tin Cấu Hình

**File cấu hình**: `application.properties`

```properties
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api
ghn.api.token=76016947-d1a8-11f0-a3d6-dac90fb956b5
ghn.shop.id=198347
ghn.pick.district.id=1485
```

**Giải thích**:
- `ghn.api.url`: Base URL của GHN API (dev environment)
- `ghn.api.token`: Token xác thực API (unique per shop)
- `ghn.shop.id`: ID của shop trên hệ thống GHN
- `ghn.pick.district.id`: ID quận/huyện nơi lấy hàng (mặc định: Hà Đông, Hà Nội)

### 1.2. API Endpoints Được Sử Dụng

#### 1.2.1. Calculate Shipping Fee
**Endpoint**: `POST /v2/shipping-order/fee`

**Purpose**: Tính phí vận chuyển dựa trên địa chỉ và trọng lượng

**Request Headers**:
```java
Token: {ghn.api.token}
ShopId: {ghn.shop.id}
Content-Type: application/json
```

**Request Body**:
```json
{
  "service_type_id": 2,
  "from_district_id": 1485,
  "to_district_id": 1454,
  "weight": 1000,
  "insurance_value": 0
}
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "total": 30000,
    "service_fee": 25000,
    "insurance_fee": 0
  }
}
```

**Implementation**: `ShippingServiceImpl.calculateShippingFee()`

#### 1.2.2. Get Lead Time (Estimated Delivery Time)
**Endpoint**: `POST /v2/shipping-order/leadtime`

**Purpose**: Lấy thời gian giao hàng dự kiến

**Request Body**:
```json
{
  "from_district_id": 1485,
  "to_district_id": 1454,
  "service_id": 2
}
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "leadtime": 1734567890
  }
}
```

**Note**: `leadtime` là Unix timestamp (seconds since epoch)

#### 1.2.3. Create Shipping Order
**Endpoint**: `POST /v2/shipping-order/create`

**Purpose**: Tạo đơn vận chuyển trên hệ thống GHN

**Request Body**:
```json
{
  "to_name": "Nguyễn Văn A",
  "to_phone": "0987654321",
  "to_address": "123 Đường ABC",
  "to_ward_code": "20308",
  "to_district_id": 1454,
  "note": "Giao giờ hành chính",
  "required_note": "KHONGCHOXEMHANG",
  "cod_amount": 500000,
  "weight": 1000,
  "length": 20,
  "width": 15,
  "height": 10,
  "service_type_id": 2,
  "payment_type_id": 1,
  "items": [
    {
      "name": "Sản phẩm A",
      "code": "SP001",
      "quantity": 2,
      "price": 250000
    }
  ]
}
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "order_code": "GHNABCD123",
    "sort_code": "HN-01-A",
    "total_fee": 30000,
    "expected_delivery_time": 1734567890
  }
}
```

**Implementation**: `ShippingServiceImpl.createGHNOrder()`

**Critical Fields**:
- `to_ward_code`: **REQUIRED** - Mã phường/xã (string)
- `required_note`: **REQUIRED** - Ghi chú bắt buộc (KHONGCHOXEMHANG, CHOXEMHANGKHONGTHU, CHOTHUHANG)
- `payment_type_id`: 1 = Shop trả phí, 2 = Người nhận trả phí

#### 1.2.4. Get Order Detail
**Endpoint**: `POST /v2/shipping-order/detail`

**Purpose**: Lấy thông tin chi tiết đơn hàng và tracking

**Request Body**:
```json
{
  "order_code": "GHNABCD123"
}
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "order_code": "GHNABCD123",
    "status": "delivering",
    "expected_delivery_time": 1734567890,
    "updated_date": 1734500000,
    "current_warehouse": "Kho Hà Nội",
    "cod_amount": 500000,
    "total_fee": 30000,
    "note": "Giao giờ hành chính",
    "log": [
      {
        "status": "picked",
        "updated_date": 1734400000,
        "location": "Hà Đông, Hà Nội"
      },
      {
        "status": "transporting",
        "updated_date": 1734450000,
        "location": "Trung tâm phân loại HN"
      }
    ]
  }
}
```

**Implementation**: `ShippingServiceImpl.getGHNOrderDetail()`

#### 1.2.5. Get Master Data (Province, District, Ward)
**Endpoints**:
- `POST /master-data/province` - Lấy danh sách tỉnh/thành
- `POST /master-data/district` - Lấy danh sách quận/huyện
- `POST /master-data/ward` - Lấy danh sách phường/xã

**Purpose**: Lấy dữ liệu địa giới hành chính để validate địa chỉ

**Implementation**: 
- `ShippingServiceImpl.getProvinces()`
- `ShippingServiceImpl.getDistricts(Integer provinceId)`
- `ShippingServiceImpl.getWards(Integer districtId)`

### 1.3. Authentication

**Method**: Token-based authentication

**Headers Required**:
```java
HttpHeaders headers = new HttpHeaders();
headers.set("Token", ghnApiToken);
headers.set("ShopId", ghnShopId.toString());
headers.setContentType(MediaType.APPLICATION_JSON);
```

**Security Considerations**:
- Token được lưu trong `application.properties` (không commit vào git)
- Token có thời hạn sử dụng, cần renew định kỳ
- Mỗi shop có token riêng, không share giữa các shop

### 1.4. Error Handling

#### 1.4.1. Common Error Codes

| Error Code | Meaning | Handling Strategy |
|------------|---------|-------------------|
| 200 | Success | Process normally |
| 400 | Bad Request | Validate input, show specific error to user |
| 401 | Unauthorized | Check token validity, renew if needed |
| 403 | Forbidden | Check shop permissions |
| 404 | Not Found | Check order code, district ID validity |
| 500 | Internal Server Error | Retry with exponential backoff |
| 503 | Service Unavailable | Queue request, retry later |

#### 1.4.2. Error Handling Implementation

```java
try {
    Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
    
    if (response != null && response.get("code").equals(200)) {
        // Success case
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        // Process data...
    } else {
        // Error case
        String message = (String) response.get("message");
        log.error("GHN API error: {}", message);
        throw new RuntimeException("GHN API error: " + message);
    }
} catch (RestClientException e) {
    log.error("Network error calling GHN API", e);
    throw new RuntimeException("Không thể kết nối đến GHN: " + e.getMessage());
}
```

#### 1.4.3. Specific Error Scenarios

**Scenario 1: Invalid Ward Code**
```
Error: "Ward code not found"
Handling: 
- Get ward list for district
- Find matching ward by name
- Use first ward as fallback
- Log warning for manual review
```

**Scenario 2: Address Not Supported**
```
Error: "Service not available for this address"
Handling:
- Keep order in READY_TO_SHIP status
- Display error to staff
- Allow manual address correction
- Retry after correction
```

**Scenario 3: Network Timeout**
```
Error: SocketTimeoutException
Handling:
- Retry up to 3 times with exponential backoff
- If all retries fail, queue for manual processing
- Send notification to admin
```

### 1.5. Webhook Integration

**Endpoint**: `POST /api/webhook/ghn`

**Purpose**: Nhận cập nhật trạng thái đơn hàng từ GHN

**Webhook Request Format**:
```json
{
  "OrderCode": "GHNABCD123",
  "Status": "delivered",
  "CODAmount": 500000,
  "Time": "2024-12-18 14:30:00",
  "Reason": "",
  "ReasonCode": ""
}
```

**GHN Status Values**:
- `ready_to_pick` - Chờ lấy hàng
- `picking` - Đang lấy hàng
- `picked` - Đã lấy hàng
- `storing` - Hàng đang nằm ở kho
- `transporting` - Đang luân chuyển
- `sorting` - Đang phân loại
- `delivering` - Đang giao hàng
- `delivered` - Đã giao hàng thành công
- `delivery_fail` - Giao hàng thất bại
- `waiting_to_return` - Chờ trả hàng
- `return` - Trả hàng
- `returned` - Đã trả hàng về shop
- `cancel` - Đơn bị hủy
- `exception` - Đơn hàng ngoại lệ
- `damage` - Hàng bị hư hỏng
- `lost` - Hàng bị thất lạc

**Status Mapping to Order Status**:

```java
switch (ghnStatus) {
    case "picked":
    case "storing":
    case "transporting":
    case "sorting":
        order.setStatus(OrderStatus.SHIPPING);
        break;
        
    case "delivering":
        order.setStatus(OrderStatus.SHIPPING);
        break;
        
    case "delivered":
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.PAID); // COD collected
        break;
        
    case "returned":
    case "cancel":
        order.setStatus(OrderStatus.CANCELLED);
        break;
}
```

**Implementation**: `WebhookServiceImpl.handleGHNWebhook()`

**Security**: 
- Webhook endpoint is public (no JWT required)
- Validate order_code exists in database
- Log all webhook requests for audit
- Consider implementing signature verification (if GHN provides)

### 1.6. Idempotency & Duplicate Prevention

**Problem**: GHN may send duplicate webhooks or retry failed requests

**Solution**:

1. **Check Current Status Before Update**:
```java
if (order.getStatus() == OrderStatus.DELIVERED) {
    log.warn("Order already delivered, ignoring webhook");
    return;
}
```

2. **Store GHN Transaction ID**:
```java
order.setGhnOrderCode(ghnOrderCode);
order.setGhnShippingStatus(ghnStatus);
```

3. **Use Database Transactions**:
```java
@Transactional
public void handleGHNWebhook(GHNWebhookRequest request) {
    // All updates in single transaction
}
```

4. **Prevent Duplicate Order Creation**:
```java
// Check if GHN order already exists
Optional<Order> existing = orderRepository.findByGhnOrderCode(ghnOrderCode);
if (existing.isPresent()) {
    throw new RuntimeException("GHN order already exists");
}
```

### 1.7. Retry Mechanism

**For API Calls**:
```java
private static final int MAX_RETRIES = 3;
private static final long RETRY_DELAY_MS = 1000;

public <T> T callWithRetry(Supplier<T> apiCall) {
    int attempt = 0;
    while (attempt < MAX_RETRIES) {
        try {
            return apiCall.get();
        } catch (RestClientException e) {
            attempt++;
            if (attempt >= MAX_RETRIES) {
                throw e;
            }
            Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
        }
    }
}
```

**For Webhooks**:
- Return HTTP 500 to trigger GHN retry
- GHN will retry failed webhooks automatically
- Log all webhook attempts for debugging

---

## 2. SePay Integration

### 2.1. Thông Tin Cấu Hình

**File cấu hình**: `application.properties`

```properties
sepay.merchant-id=SP-TEST-LQB926AA
sepay.secret-key=spsk_test_G9NcwYj2Qd2HK32rWxLd9zazg38DQSUE
sepay.api-url=https://api-staging.sepay.vn/v2/payment-qr

sepay.bank.code=MBBank
sepay.bank.account.number=3333315012003
sepay.bank.account.name=LE MINH VUONG

sepay.api.secret=spsk_test_G9NcwYj2Qd2HK32rWxLd9zazg38DQSUE
sepay.amount.multiplier=1
```

**Giải thích**:
- `sepay.merchant-id`: ID merchant trên hệ thống SePay
- `sepay.secret-key`: Secret key để xác thực API
- `sepay.api-url`: Base URL của SePay API (staging environment)
- `sepay.bank.*`: Thông tin tài khoản ngân hàng nhận tiền
- `sepay.api.secret`: Secret key để verify webhook signature
- `sepay.amount.multiplier`: Hệ số nhân số tiền (1 = giữ nguyên)

### 2.2. QR Code Generation

**Method**: VietQR Standard

**Implementation**:
```java
private String generateSepayQrCode(String content, Double amount, 
                                   String bankCode, String accountNumber, 
                                   String accountName) {
    long amountInVnd = Math.round(amount * amountMultiplier);
    
    return String.format(
        "https://img.vietqr.io/image/%s-%s-qr_only.jpg?amount=%d&addInfo=%s&accountName=%s",
        bankCode,
        accountNumber,
        amountInVnd,
        content,
        accountName.replace(" ", "%20")
    );
}
```

**QR Code URL Format**:
```
https://img.vietqr.io/image/{BANK_CODE}-{ACCOUNT_NUMBER}-qr_only.jpg
  ?amount={AMOUNT}
  &addInfo={PAYMENT_CODE}
  &accountName={ACCOUNT_NAME}
```

**Example**:
```
https://img.vietqr.io/image/MBBank-3333315012003-qr_only.jpg
  ?amount=500000
  &addInfo=PAY202412180001
  &accountName=LE%20MINH%20VUONG
```

**QR Code Templates**:
- `qr_only` - Chỉ có mã QR (clean, minimal)
- `compact` - QR + thông tin ngắn gọn
- `compact2` - QR + thông tin chi tiết
- `print` - Định dạng in ấn

**Supported Banks**:
- MBBank (MB Bank)
- VCB (Vietcombank)
- TCB (Techcombank)
- ACB (ACB)
- VPBank (VPBank)
- Vietinbank
- BIDV
- Agribank
- And more...

### 2.3. Payment Flow

**Step 1: Create Payment**
```java
// 1. Generate unique payment code
String paymentCode = "PAY" + LocalDate.now().format("yyyyMMdd") + randomNumber;

// 2. Generate QR code URL
String qrCodeUrl = generateSepayQrCode(paymentCode, amount, bankCode, accountNumber, accountName);

// 3. Create payment record
Payment payment = Payment.builder()
    .paymentCode(paymentCode)
    .order(order)
    .amount(amount)
    .method(PaymentMethod.SEPAY)
    .status(PaymentStatus.PENDING)
    .sepayBankCode(bankCode)
    .sepayAccountNumber(accountNumber)
    .sepayAccountName(accountName)
    .sepayContent(paymentCode)
    .sepayQrCode(qrCodeUrl)
    .expiredAt(LocalDateTime.now().plusMinutes(15))
    .build();

// 4. Update order status
order.setStatus(OrderStatus.PENDING_PAYMENT);
order.setPaymentStatus(PaymentStatus.PENDING);
```

**Step 2: Customer Scans QR & Transfers**
- Customer opens banking app
- Scans QR code
- Confirms transfer with pre-filled information
- Bank processes transfer

**Step 3: SePay Webhook Notification**
- SePay monitors bank account transactions
- Detects incoming transfer matching payment code
- Sends webhook to system
- System verifies and updates payment status

### 2.4. Webhook Integration

**Endpoint**: `POST /api/payment/sepay/webhook`

**Purpose**: Nhận thông báo thanh toán từ SePay

**Webhook Request Format**:
```json
{
  "transactionId": "FT24351234567890",
  "amount": 500000,
  "content": "PAY202412180001 FT2533",
  "bankCode": "MBBank",
  "accountNumber": "3333315012003",
  "transactionDate": "2024-12-18 14:30:00",
  "signature": "abc123def456..."
}
```

**Implementation**: `PaymentServiceImpl.handleSepayWebhook()`

**Processing Steps**:

1. **Extract Payment Code**:
```java
private String extractPaymentCode(String content) {
    // Content may be: "PAY202412180001" or "PAY202412180001 FT2533.."
    int index = content.indexOf("PAY");
    if (index != -1) {
        int endIndex = Math.min(index + 15, content.length());
        return content.substring(index, endIndex).split("\\s+")[0];
    }
    return content.trim();
}
```

2. **Find Payment Record**:
```java
Payment payment = paymentRepository.findByPaymentCode(paymentCode)
    .orElseThrow(() -> new RuntimeException("Payment not found"));
```

3. **Verify Signature** (if configured):
```java
private boolean verifySignature(SepayWebhookRequest request, String apiToken) {
    String data = request.getTransactionId() + 
                  request.getAmount() + 
                  request.getContent() + 
                  apiToken;
    String calculatedSignature = DigestUtils.sha256Hex(data);
    return calculatedSignature.equals(request.getSignature());
}
```

4. **Validate Amount**:
```java
if (!payment.getAmount().equals(request.getAmount())) {
    log.error("Amount mismatch. Expected: {}, Received: {}", 
              payment.getAmount(), request.getAmount());
    return ApiResponse.error("Số tiền không khớp");
}
```

5. **Check Expiration**:
```java
if (LocalDateTime.now().isAfter(payment.getExpiredAt())) {
    payment.setStatus(PaymentStatus.EXPIRED);
    return ApiResponse.error("Thanh toán đã hết hạn");
}
```

6. **Update Payment & Order**:
```java
// Update payment
payment.setStatus(PaymentStatus.SUCCESS);
payment.setSepayTransactionId(request.getTransactionId());
payment.setPaidAt(LocalDateTime.now());
paymentRepository.save(payment);

// Update order
order.setPaymentStatus(PaymentStatus.PAID);
order.setStatus(OrderStatus.CONFIRMED);
order.setConfirmedAt(LocalDateTime.now());
orderRepository.save(order);

// Publish event for accounting
eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, oldStatus, newStatus));
```

### 2.5. Security - Webhook Signature Verification

**Purpose**: Đảm bảo webhook thực sự đến từ SePay, không phải từ attacker

**Signature Algorithm**: SHA-256 HMAC

**Implementation**:
```java
import org.apache.commons.codec.digest.DigestUtils;

private boolean verifySignature(SepayWebhookRequest request, String apiToken) {
    // Concatenate data in specific order
    String data = request.getTransactionId() + 
                  request.getAmount() + 
                  request.getContent() + 
                  apiToken;
    
    // Calculate SHA-256 hash
    String calculatedSignature = DigestUtils.sha256Hex(data);
    
    // Compare with received signature
    return calculatedSignature.equals(request.getSignature());
}
```

**Security Best Practices**:
1. Always verify signature before processing payment
2. Use constant-time comparison to prevent timing attacks
3. Log all webhook requests (including failed verifications)
4. Rate limit webhook endpoint to prevent DoS
5. Store API secret securely (environment variables, not in code)

**Current Implementation Status**:
```java
// TODO: Implement real signature verification
// Currently returns true for demo purposes
log.info("Verifying signature with API token: {}...", 
         apiToken.substring(0, Math.min(10, apiToken.length())));
return true;
```

### 2.6. Idempotency & Duplicate Prevention

**Problem**: SePay may send duplicate webhooks for same transaction

**Solutions**:

1. **Check Payment Status**:
```java
if (payment.getStatus() == PaymentStatus.SUCCESS) {
    log.warn("Payment already processed: {}", payment.getPaymentCode());
    return ApiResponse.success("Thanh toán đã được xử lý");
}
```

2. **Store Transaction ID**:
```java
Optional<Payment> existing = paymentRepository
    .findBySepayTransactionId(request.getTransactionId());
if (existing.isPresent()) {
    return ApiResponse.success("Transaction already processed");
}
```

3. **Use Database Transaction**:
```java
@Transactional
public ApiResponse handleSepayWebhook(SepayWebhookRequest request) {
    // All updates in single transaction
    // Rollback if any step fails
}
```

4. **Optimistic Locking**:
```java
@Entity
public class Payment {
    @Version
    private Long version;
    // Prevents concurrent updates
}
```

### 2.7. Payment Expiration

**Timeout**: 15 minutes from creation

**Scheduler Job**:
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void expireOldPayments() {
    LocalDateTime now = LocalDateTime.now();
    List<Payment> expiredPayments = paymentRepository
        .findByStatusAndExpiredAtBefore(PaymentStatus.PENDING, now);
    
    for (Payment payment : expiredPayments) {
        payment.setStatus(PaymentStatus.EXPIRED);
        payment.setFailureReason("Hết hạn thanh toán");
        paymentRepository.save(payment);
        
        // Cancel order
        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(now);
            order.setCancelReason("Hết hạn thanh toán");
            orderRepository.save(order);
        }
    }
}
```

**Implementation**: `PaymentScheduler.expireOldPayments()`

### 2.8. Multi-Account Banking Support

**Feature**: Hỗ trợ nhiều tài khoản ngân hàng

**Database Schema**:
```sql
CREATE TABLE bank_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_code VARCHAR(20) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    sepay_api_token VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Usage**:
```java
// Get default bank account
BankAccount bankAccount = bankAccountRepository
    .findByIsDefaultTrue()
    .orElse(null);

// Fallback to config if no default
String bankCode = bankAccount != null ? 
    bankAccount.getBankCode() : sepayBankCode;
```

**Benefits**:
- Support multiple payment channels
- Load balancing across accounts
- Separate accounts for different business units
- Easy account switching without code changes

---

## 3. Cloudinary Integration

### 3.1. Thông Tin Cấu Hình

**File cấu hình**: `application.properties`

```properties
cloudinary.url=cloudinary://751297543476923:K0M-9VnoYv0U4B_UwMkdyUgyVEQ@dyi3zgja2
cloudinary.upload-preset=ml_default
```

**Alternative Configuration**:
```properties
cloudinary.cloud-name=dyi3zgja2
cloudinary.api-key=751297543476923
cloudinary.api-secret=K0M-9VnoYv0U4B_UwMkdyUgyVEQ
```

**Giải thích**:
- `cloudinary.url`: Complete connection URL (recommended method)
- `cloudinary.cloud-name`: Cloud name (account identifier)
- `cloudinary.api-key`: API key for authentication
- `cloudinary.api-secret`: API secret for authentication
- `cloudinary.upload-preset`: Upload preset for unsigned uploads

### 3.2. Configuration Bean

**Implementation**: `CloudinaryConfig.java`

```java
@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;
    
    @Value("${cloudinary.cloud-name:}")
    private String cloudName;
    
    @Value("${cloudinary.api-key:}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret:}")
    private String apiSecret;
    
    @Bean
    public Cloudinary cloudinary() {
        // Priority: Use CLOUDINARY_URL if available
        if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
            return new Cloudinary(cloudinaryUrl);
        }
        
        // Fallback: Use individual config
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }
}
```

### 3.3. Image Upload

**Endpoint**: `POST /api/files/upload`

**Implementation**: `CloudinaryServiceImpl.uploadImage()`

**Upload Process**:

1. **Validate File**:
```java
// Check if file is empty
if (file.isEmpty()) {
    throw new RuntimeException("File không được để trống");
}

// Validate file type
String contentType = file.getContentType();
if (contentType == null || !contentType.startsWith("image/")) {
    throw new RuntimeException("File phải là ảnh (PNG, JPG, GIF)");
}

// Validate file size (max 10MB)
if (file.getSize() > 10 * 1024 * 1024) {
    throw new RuntimeException("Kích thước ảnh không được vượt quá 10MB");
}
```

2. **Upload to Cloudinary**:
```java
Map<String, Object> uploadResult = cloudinary.uploader().upload(
    file.getBytes(), 
    ObjectUtils.asMap(
        "folder", "products",
        "resource_type", "image"
    )
);

String imageUrl = (String) uploadResult.get("secure_url");
```

3. **Return Secure URL**:
```java
return imageUrl;
// Example: https://res.cloudinary.com/dyi3zgja2/image/upload/v1734567890/products/abc123.jpg
```

**Upload Options**:
- `folder`: Organize images in folders (e.g., "products", "categories")
- `resource_type`: Type of resource ("image", "video", "raw")
- `public_id`: Custom identifier for the image
- `transformation`: Apply transformations (resize, crop, etc.)
- `tags`: Add tags for organization
- `context`: Add custom metadata

**Advanced Upload with Transformations**:
```java
Map<String, Object> uploadResult = cloudinary.uploader().upload(
    file.getBytes(),
    ObjectUtils.asMap(
        "folder", "products",
        "transformation", new Transformation()
            .width(800).height(600).crop("limit")
            .quality("auto")
            .fetchFormat("auto")
    )
);
```

### 3.4. Image Deletion

**Implementation**: `CloudinaryServiceImpl.deleteImage()`

**Deletion Process**:

1. **Extract Public ID from URL**:
```java
public String extractPublicId(String imageUrl) {
    // URL: https://res.cloudinary.com/demo/image/upload/v1234567890/products/abc123.jpg
    // Public ID: products/abc123
    
    String[] parts = imageUrl.split("/upload/");
    if (parts.length < 2) return null;
    
    String pathWithVersion = parts[1];
    String path = pathWithVersion.replaceFirst("v\\d+/", "");
    
    int lastDot = path.lastIndexOf('.');
    if (lastDot > 0) {
        path = path.substring(0, lastDot);
    }
    
    return path;
}
```

2. **Delete from Cloudinary**:
```java
public void deleteImage(String publicId) {
    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    log.info("Deleted image from Cloudinary: {}", publicId);
}
```

**Usage Example**:
```java
String imageUrl = "https://res.cloudinary.com/.../products/abc123.jpg";
String publicId = cloudinaryService.extractPublicId(imageUrl);
cloudinaryService.deleteImage(publicId);
```

### 3.5. Image Transformations

**On-the-fly Transformations**: Cloudinary supports URL-based transformations

**Examples**:

1. **Resize to 300x300**:
```
https://res.cloudinary.com/demo/image/upload/w_300,h_300,c_fill/products/abc123.jpg
```

2. **Auto Quality & Format**:
```
https://res.cloudinary.com/demo/image/upload/q_auto,f_auto/products/abc123.jpg
```

3. **Thumbnail with Crop**:
```
https://res.cloudinary.com/demo/image/upload/w_200,h_200,c_thumb,g_face/products/abc123.jpg
```

4. **Watermark**:
```
https://res.cloudinary.com/demo/image/upload/l_watermark,g_south_east,x_10,y_10/products/abc123.jpg
```

**Transformation Parameters**:
- `w` - Width
- `h` - Height
- `c` - Crop mode (fill, fit, limit, scale, thumb, etc.)
- `q` - Quality (auto, 80, 90, etc.)
- `f` - Format (auto, jpg, png, webp, etc.)
- `g` - Gravity (center, face, north, south, etc.)
- `e` - Effects (blur, grayscale, sepia, etc.)

### 3.6. Error Handling

**Common Errors**:

1. **Invalid Credentials**:
```
Error: "Invalid cloud_name or API credentials"
Handling: Check configuration, verify credentials
```

2. **File Too Large**:
```
Error: "File size exceeds maximum allowed"
Handling: Validate file size before upload (max 10MB)
```

3. **Invalid File Type**:
```
Error: "Unsupported file type"
Handling: Validate content type (only images allowed)
```

4. **Network Timeout**:
```
Error: "Connection timeout"
Handling: Retry with exponential backoff
```

**Error Handling Implementation**:
```java
try {
    String imageUrl = cloudinaryService.uploadImage(file);
    return ApiResponse.success("Upload thành công", imageUrl);
} catch (RuntimeException e) {
    log.error("Upload failed", e);
    return ApiResponse.error("Upload thất bại: " + e.getMessage());
} catch (IOException e) {
    log.error("Network error", e);
    return ApiResponse.error("Lỗi kết nối: " + e.getMessage());
}
```

### 3.7. Security Considerations

**1. Signed Uploads** (for sensitive operations):
```java
Map<String, Object> uploadResult = cloudinary.uploader().upload(
    file.getBytes(),
    ObjectUtils.asMap(
        "folder", "products",
        "signature", generateSignature(),
        "timestamp", System.currentTimeMillis() / 1000
    )
);
```

**2. Upload Presets** (for unsigned uploads):
- Configure preset in Cloudinary dashboard
- Restrict allowed formats, sizes, transformations
- Enable moderation for user-generated content

**3. Access Control**:
- Use secure URLs (HTTPS)
- Implement authentication on upload endpoint
- Validate user permissions before upload/delete

**4. Content Moderation**:
```java
Map<String, Object> uploadResult = cloudinary.uploader().upload(
    file.getBytes(),
    ObjectUtils.asMap(
        "folder", "products",
        "moderation", "manual" // or "aws_rek" for AI moderation
    )
);
```

### 3.8. Best Practices

**1. Organize with Folders**:
```
/products - Product images
/categories - Category images
/users - User avatars
/banners - Marketing banners
```

**2. Use Descriptive Public IDs**:
```java
String publicId = "products/" + productId + "_" + timestamp;
```

**3. Implement Cleanup**:
```java
// Delete old image when updating product
if (product.getImageUrl() != null) {
    String oldPublicId = cloudinaryService.extractPublicId(product.getImageUrl());
    cloudinaryService.deleteImage(oldPublicId);
}
```

**4. Optimize Delivery**:
- Use `q_auto` for automatic quality optimization
- Use `f_auto` for automatic format selection (WebP for modern browsers)
- Enable CDN caching

**5. Monitor Usage**:
- Track upload count and bandwidth
- Set up alerts for quota limits
- Implement rate limiting on upload endpoint

---

## 4. Cross-Cutting Concerns

### 4.1. Logging Strategy

**Log Levels**:
- `INFO` - Successful operations, important state changes
- `WARN` - Recoverable errors, fallback scenarios
- `ERROR` - Failed operations, exceptions

**Logging Examples**:

```java
// GHN Integration
log.info("Creating GHN order for order: {}", orderId);
log.info("GHN order created successfully: {}", ghnOrderCode);
log.warn("Ward code not found, using fallback");
log.error("Failed to create GHN order: {}", e.getMessage());

// SePay Integration
log.info("Received SePay webhook: {}", request);
log.warn("Payment already processed: {}", paymentCode);
log.error("Invalid signature from SePay webhook");

// Cloudinary Integration
log.info("Uploaded image to Cloudinary: {}", imageUrl);
log.warn("Could not delete image: {}", publicId);
log.error("Error uploading to Cloudinary", e);
```

**Sensitive Data Handling**:
```java
// Mask sensitive data in logs
log.info("Using API token: {}...", token.substring(0, 10));
log.info("Bank account: {} - {} - {}", bankCode, 
         accountNumber.replaceAll("\\d(?=\\d{4})", "*"), accountName);
```

### 4.2. Monitoring & Alerting

**Metrics to Track**:

1. **GHN Integration**:
   - API call success rate
   - Average response time
   - Failed order creation count
   - Webhook processing time

2. **SePay Integration**:
   - Payment success rate
   - Average payment processing time
   - Webhook verification failures
   - Expired payment count

3. **Cloudinary Integration**:
   - Upload success rate
   - Average upload time
   - Storage usage
   - Bandwidth usage

**Implementation with Micrometer** (example):
```java
@Service
public class MetricsService {
    private final MeterRegistry registry;
    
    public void recordGHNApiCall(boolean success, long duration) {
        registry.counter("ghn.api.calls", 
            "success", String.valueOf(success)).increment();
        registry.timer("ghn.api.duration").record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void recordPaymentProcessed(boolean success) {
        registry.counter("payment.processed", 
            "success", String.valueOf(success)).increment();
    }
}
```

### 4.3. Configuration Management

**Environment-Specific Configuration**:

```properties
# Development
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api
sepay.api-url=https://api-staging.sepay.vn/v2/payment-qr

# Production
ghn.api.url=https://online-gateway.ghn.vn/shiip/public-api
sepay.api-url=https://api.sepay.vn/v2/payment-qr
```

**Externalized Configuration**:
- Use environment variables for sensitive data
- Use Spring Cloud Config for centralized configuration
- Use Kubernetes ConfigMaps/Secrets for container deployments

**Configuration Validation**:
```java
@Configuration
@Validated
public class GHNConfig {
    @NotBlank(message = "GHN API URL is required")
    @Value("${ghn.api.url}")
    private String apiUrl;
    
    @NotBlank(message = "GHN API token is required")
    @Value("${ghn.api.token}")
    private String apiToken;
}
```

### 4.4. Testing Strategy

**Unit Tests**:
```java
@Test
public void testExtractPaymentCode() {
    String content = "PAY202412180001 FT2533";
    String result = paymentService.extractPaymentCode(content);
    assertEquals("PAY202412180001", result);
}

@Test
public void testGenerateQRCode() {
    String qrUrl = paymentService.generateSepayQrCode(
        "PAY202412180001", 500000.0, "MBBank", "123456", "TEST"
    );
    assertTrue(qrUrl.contains("img.vietqr.io"));
    assertTrue(qrUrl.contains("amount=500000"));
}
```

**Integration Tests with WireMock**:
```java
@Test
public void testGHNCreateOrder() {
    // Mock GHN API response
    stubFor(post(urlEqualTo("/v2/shipping-order/create"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"code\":200,\"data\":{\"order_code\":\"GHN123\"}}")));
    
    // Test service
    CreateGHNOrderResponse response = shippingService.createGHNOrder(request);
    assertEquals("GHN123", response.getOrderCode());
}
```

**End-to-End Tests**:
```java
@Test
@Transactional
public void testCompletePaymentFlow() {
    // 1. Create order
    Order order = createTestOrder();
    
    // 2. Create payment
    Payment payment = paymentService.createPayment(order);
    assertEquals(PaymentStatus.PENDING, payment.getStatus());
    
    // 3. Simulate webhook
    SepayWebhookRequest webhook = createWebhookRequest(payment);
    paymentService.handleSepayWebhook(webhook);
    
    // 4. Verify payment completed
    Payment updated = paymentRepository.findById(payment.getId()).get();
    assertEquals(PaymentStatus.SUCCESS, updated.getStatus());
    
    // 5. Verify order confirmed
    Order updatedOrder = orderRepository.findById(order.getId()).get();
    assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
}
```

### 4.5. Error Recovery Strategies

**1. Circuit Breaker Pattern** (for external API calls):
```java
@Service
public class ResilientShippingService {
    @CircuitBreaker(name = "ghn", fallbackMethod = "createGHNOrderFallback")
    public CreateGHNOrderResponse createGHNOrder(CreateGHNOrderRequest request) {
        return shippingService.createGHNOrder(request);
    }
    
    private CreateGHNOrderResponse createGHNOrderFallback(
            CreateGHNOrderRequest request, Exception e) {
        log.error("GHN service unavailable, queuing order", e);
        queueService.queueGHNOrder(request);
        throw new ServiceUnavailableException("GHN temporarily unavailable");
    }
}
```

**2. Retry with Exponential Backoff**:
```java
@Retryable(
    value = {RestClientException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public Map<String, Object> callGHNApi(String url, Object request) {
    return restTemplate.postForObject(url, request, Map.class);
}
```

**3. Dead Letter Queue** (for failed webhooks):
```java
@Service
public class WebhookProcessor {
    public void processWebhook(WebhookRequest request) {
        try {
            handleWebhook(request);
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            deadLetterQueue.add(request);
        }
    }
}
```

---

## 5. Tổng Kết

### 5.1. Điểm Mạnh

**GHN Integration**:
- ✅ Comprehensive API coverage (fee calculation, order creation, tracking)
- ✅ Robust error handling with fallback mechanisms
- ✅ Webhook integration for real-time status updates
- ✅ Master data caching for performance

**SePay Integration**:
- ✅ Simple QR code generation using VietQR standard
- ✅ Multi-account banking support
- ✅ Automatic payment expiration handling
- ✅ Event-driven architecture for accounting integration

**Cloudinary Integration**:
- ✅ Easy image upload with validation
- ✅ Secure URL generation
- ✅ Flexible transformation support
- ✅ Proper cleanup on image deletion

### 5.2. Điểm Cần Cải Thiện

**Security**:
- ⚠️ SePay webhook signature verification not fully implemented
- ⚠️ GHN webhook lacks signature verification
- ⚠️ API credentials stored in properties file (should use environment variables)

**Reliability**:
- ⚠️ No circuit breaker pattern implemented
- ⚠️ Limited retry mechanism for failed API calls
- ⚠️ No dead letter queue for failed webhooks

**Monitoring**:
- ⚠️ No metrics collection for API calls
- ⚠️ No alerting for service degradation
- ⚠️ Limited logging of business events

**Testing**:
- ⚠️ No integration tests with external services
- ⚠️ No load testing for webhook endpoints
- ⚠️ No chaos engineering tests

### 5.3. Đề Xuất Cải Tiến

**Ngắn Hạn** (1-2 tháng):
1. Implement SePay webhook signature verification
2. Move API credentials to environment variables
3. Add comprehensive logging for all external API calls
4. Implement rate limiting on webhook endpoints

**Trung Hạn** (3-6 tháng):
1. Implement circuit breaker pattern with Resilience4j
2. Add metrics collection with Micrometer
3. Set up monitoring dashboard with Grafana
4. Implement dead letter queue for failed webhooks
5. Add integration tests with WireMock

**Dài Hạn** (6-12 tháng):
1. Implement event sourcing for payment and shipping events
2. Add distributed tracing with Zipkin/Jaeger
3. Implement chaos engineering tests
4. Add automated failover for external services
5. Implement caching layer for frequently accessed data

### 5.4. Bảng So Sánh Các Dịch Vụ

| Tiêu Chí | GHN | SePay | Cloudinary |
|----------|-----|-------|------------|
| **Authentication** | Token-based | API Key | API Key + Secret |
| **Webhook Support** | ✅ Yes | ✅ Yes | ✅ Yes (optional) |
| **Signature Verification** | ❌ Not implemented | ⚠️ Partial | ✅ Yes |
| **Retry Mechanism** | ⚠️ Manual | ⚠️ Manual | ✅ Built-in |
| **Rate Limiting** | ✅ Yes (by GHN) | ✅ Yes (by SePay) | ✅ Yes (by Cloudinary) |
| **Error Handling** | ✅ Comprehensive | ✅ Good | ✅ Good |
| **Documentation** | ✅ Good | ⚠️ Limited | ✅ Excellent |
| **Sandbox Environment** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Cost** | Pay per shipment | Transaction fee | Pay per usage |

### 5.5. Kết Luận

Hệ thống đã tích hợp thành công với 3 dịch vụ bên ngoài quan trọng:
- **GHN** cho vận chuyển
- **SePay** cho thanh toán online
- **Cloudinary** cho quản lý hình ảnh

Các tích hợp này đã được implement với:
- ✅ Error handling cơ bản
- ✅ Logging đầy đủ
- ✅ Webhook integration
- ✅ Idempotency handling

Tuy nhiên, vẫn cần cải thiện về:
- Security (signature verification)
- Reliability (circuit breaker, retry)
- Monitoring (metrics, alerting)
- Testing (integration tests, load tests)

Với các đề xuất cải tiến trên, hệ thống sẽ trở nên robust và production-ready hơn.

---

## Phụ Lục

### A. Danh Sách API Endpoints

**GHN**:
- `POST /v2/shipping-order/fee` - Calculate shipping fee
- `POST /v2/shipping-order/leadtime` - Get delivery time
- `POST /v2/shipping-order/create` - Create shipping order
- `POST /v2/shipping-order/detail` - Get order detail
- `POST /master-data/province` - Get provinces
- `POST /master-data/district` - Get districts
- `POST /master-data/ward` - Get wards

**SePay**:
- Webhook: `POST /api/payment/sepay/webhook` - Receive payment notification

**Cloudinary**:
- Upload: `cloudinary.uploader().upload()` - Upload image
- Delete: `cloudinary.uploader().destroy()` - Delete image

### B. Configuration Checklist

**Before Deployment**:
- [ ] Update GHN API token
- [ ] Update SePay API credentials
- [ ] Update Cloudinary credentials
- [ ] Configure webhook URLs
- [ ] Set up SSL certificates
- [ ] Configure firewall rules
- [ ] Set up monitoring
- [ ] Configure backup strategy
- [ ] Test all integrations in staging
- [ ] Prepare rollback plan

### C. Troubleshooting Guide

**GHN Issues**:
- Problem: "Invalid token"
  - Solution: Check token validity, renew if expired
- Problem: "Ward code not found"
  - Solution: Use getWards() API to find correct code
- Problem: "Service not available"
  - Solution: Check address, try different service type

**SePay Issues**:
- Problem: "Payment not detected"
  - Solution: Check payment code in transfer content
- Problem: "Amount mismatch"
  - Solution: Verify amount multiplier configuration
- Problem: "Webhook not received"
  - Solution: Check webhook URL, firewall rules

**Cloudinary Issues**:
- Problem: "Upload failed"
  - Solution: Check file size, type, credentials
- Problem: "Image not found"
  - Solution: Verify public_id extraction logic
- Problem: "Quota exceeded"
  - Solution: Upgrade plan or optimize usage

---

**Tài liệu này được tạo cho mục đích phân tích và báo cáo đồ án tốt nghiệp.**

**Ngày tạo**: 2024-12-23

**Phiên bản**: 1.0
