# 🔔 HƯỚNG DẪN WEBHOOK GHN - CẬP NHẬT TRẠNG THÁI TỰ ĐỘNG

## 📋 TỔNG QUAN

Webhook GHN cho phép hệ thống **tự động nhận thông báo** khi có thay đổi trạng thái đơn hàng, bao gồm:
- ✅ Tài xế đến lấy hàng
- ✅ Đang vận chuyển
- ✅ Đã giao hàng thành công
- ✅ Giao hàng thất bại
- ✅ Đang hoàn trả

---

## 🔧 CẤU HÌNH WEBHOOK

### **1. Đăng ký Webhook URL với GHN**

Truy cập: https://khachhang.giaohangnhanh.vn/

1. Đăng nhập tài khoản GHN
2. Vào **Cài đặt** → **Webhook**
3. Thêm URL webhook của bạn:

```
https://your-domain.com/api/webhooks/ghn
```

4. Chọn các sự kiện muốn nhận:
   - ✅ Đơn hàng đã được lấy
   - ✅ Đơn hàng đang giao
   - ✅ Đơn hàng đã giao thành công
   - ✅ Đơn hàng giao thất bại
   - ✅ Đơn hàng đang hoàn trả

---

## 📡 WEBHOOK ENDPOINT ĐÃ CÓ

### **Backend: WebhookController.java**

```java
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    @PostMapping("/ghn")
    public ResponseEntity<?> handleGHNWebhook(@RequestBody GHNWebhookRequest request) {
        // Xử lý webhook từ GHN
        // Cập nhật trạng thái đơn hàng tự động
    }
}
```

**File:** `src/main/java/com/doan/WEB_TMDT/module/webhook/controller/WebhookController.java`

---

## 📦 CẤU TRÚC WEBHOOK GHN GỬI ĐẾN

### **Request Body:**

```json
{
  "OrderCode": "GHN_ORDER_CODE",
  "Status": "picking",
  "StatusText": "Đang lấy hàng",
  "Time": "2024-12-20T10:30:00",
  "Location": "Kho Hà Nội",
  "Reason": "",
  "ReasonCode": "",
  "Weight": 1000,
  "Fee": 25000,
  "CODAmount": 500000,
  "CODTransferDate": null
}
```

### **Các Status quan trọng:**

| Status | StatusText | Ý nghĩa |
|--------|-----------|---------|
| `ready_to_pick` | Chờ lấy hàng | Đơn đã tạo, chờ tài xế đến lấy |
| `picking` | **Đang lấy hàng** | **Tài xế đang đến lấy hàng** ⭐ |
| `picked` | Đã lấy hàng | Tài xế đã lấy hàng thành công |
| `storing` | Hàng đang ở kho | Hàng đang nằm ở kho trung chuyển |
| `transporting` | Đang luân chuyển | Đang vận chuyển giữa các kho |
| `delivering` | **Đang giao hàng** | **Tài xế đang giao cho khách** ⭐ |
| `delivered` | **Đã giao hàng** | **Giao thành công** ✅ |
| `delivery_fail` | Giao hàng thất bại | Không giao được, sẽ giao lại |
| `return` | Đang hoàn trả | Đang trả hàng về shop |
| `returned` | Đã hoàn trả | Đã trả hàng về shop |
| `cancel` | Đã hủy | Đơn hàng bị hủy |

---

## 🔄 LUỒNG CẬP NHẬT TỰ ĐỘNG

### **Khi tài xế đến lấy hàng:**

```
1. Tài xế GHN đến shop lấy hàng
   ↓
2. GHN gửi webhook: status = "picking"
   POST https://your-domain.com/api/webhooks/ghn
   ↓
3. Backend nhận webhook
   ↓
4. Tìm Order theo ghnOrderCode
   ↓
5. Cập nhật:
   - order.ghnShippingStatus = "picking"
   - order.status = SHIPPING (nếu chưa)
   - order.shippedAt = now()
   ↓
6. Lưu vào database
   ↓
7. Frontend tự động hiển thị "Đang lấy hàng"
```

### **Khi đang giao hàng:**

```
1. Tài xế đang giao hàng cho khách
   ↓
2. GHN gửi webhook: status = "delivering"
   ↓
3. Backend cập nhật:
   - order.ghnShippingStatus = "delivering"
   - order.status = SHIPPING
   ↓
4. Frontend hiển thị "Đang giao hàng"
```

### **Khi giao thành công:**

```
1. Khách nhận hàng thành công
   ↓
2. GHN gửi webhook: status = "delivered"
   ↓
3. Backend cập nhật:
   - order.ghnShippingStatus = "delivered"
   - order.status = DELIVERED
   - order.deliveredAt = now()
   - order.paymentStatus = PAID (nếu COD)
   ↓
4. Frontend hiển thị "Đã giao hàng"
```

---

## 💻 CODE IMPLEMENTATION

### **1. WebhookController.java**

```java
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @PostMapping("/ghn")
    public ResponseEntity<?> handleGHNWebhook(@RequestBody GHNWebhookRequest request) {
        try {
            log.info("=== GHN Webhook Received ===");
            log.info("OrderCode: {}", request.getOrderCode());
            log.info("Status: {}", request.getStatus());
            log.info("StatusText: {}", request.getStatusText());
            log.info("Time: {}", request.getTime());
            log.info("Location: {}", request.getLocation());
            
            // Process webhook
            webhookService.processGHNWebhook(request);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Webhook processed"));
            
        } catch (Exception e) {
            log.error("Error processing GHN webhook", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
```

### **2. WebhookService.java**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {
    
    private final OrderRepository orderRepository;
    
    @Override
    @Transactional
    public void processGHNWebhook(GHNWebhookRequest request) {
        // Find order by GHN order code
        Order order = orderRepository.findByGhnOrderCode(request.getOrderCode())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderCode()));
        
        log.info("Processing webhook for order: {}", order.getOrderCode());
        
        // Update GHN shipping status
        order.setGhnShippingStatus(request.getStatus());
        
        // Update order status based on GHN status
        switch (request.getStatus()) {
            case "picking":
                // Tài xế đang đến lấy hàng
                if (order.getStatus() == OrderStatus.CONFIRMED) {
                    order.setStatus(OrderStatus.SHIPPING);
                    order.setShippedAt(LocalDateTime.now());
                }
                break;
                
            case "picked":
                // Đã lấy hàng thành công
                if (order.getStatus() == OrderStatus.CONFIRMED) {
                    order.setStatus(OrderStatus.SHIPPING);
                    order.setShippedAt(LocalDateTime.now());
                }
                break;
                
            case "delivering":
                // Đang giao hàng cho khách
                order.setStatus(OrderStatus.SHIPPING);
                break;
                
            case "delivered":
                // Đã giao hàng thành công
                order.setStatus(OrderStatus.DELIVERED);
                order.setDeliveredAt(LocalDateTime.now());
                
                // Nếu COD, đánh dấu đã thanh toán
                if ("COD".equals(order.getPaymentMethod())) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                }
                break;
                
            case "delivery_fail":
                // Giao hàng thất bại
                log.warn("Delivery failed for order {}: {}", order.getOrderCode(), request.getReason());
                break;
                
            case "return":
            case "returned":
                // Đang hoàn trả / Đã hoàn trả
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason("Hoàn trả từ GHN: " + request.getReason());
                break;
                
            case "cancel":
                // Đơn bị hủy
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason("Hủy từ GHN: " + request.getReason());
                break;
        }
        
        orderRepository.save(order);
        
        log.info("✅ Updated order {} status to {}", order.getOrderCode(), order.getStatus());
    }
}
```

### **3. GHNWebhookRequest.java (DTO)**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GHNWebhookRequest {
    private String OrderCode;      // Mã vận đơn GHN
    private String Status;         // picking, delivering, delivered, etc.
    private String StatusText;     // Đang lấy hàng, Đang giao hàng, etc.
    private String Time;           // Thời gian cập nhật
    private String Location;       // Vị trí hiện tại
    private String Reason;         // Lý do (nếu có)
    private String ReasonCode;     // Mã lý do
    private Integer Weight;        // Cân nặng
    private Double Fee;            // Phí vận chuyển
    private Double CODAmount;      // Tiền COD
    private String CODTransferDate; // Ngày chuyển tiền COD
}
```

---

## 🧪 TEST WEBHOOK

### **1. Test bằng Postman:**

```http
POST http://localhost:8080/api/webhooks/ghn
Content-Type: application/json

{
  "OrderCode": "GHN_ORDER_CODE",
  "Status": "picking",
  "StatusText": "Đang lấy hàng",
  "Time": "2024-12-20T10:30:00",
  "Location": "Kho Hà Nội",
  "Reason": "",
  "ReasonCode": "",
  "Weight": 1000,
  "Fee": 25000,
  "CODAmount": 500000,
  "CODTransferDate": null
}
```

### **2. Test các trạng thái:**

**Test 1: Tài xế đến lấy hàng**
```json
{
  "OrderCode": "GHN123456",
  "Status": "picking",
  "StatusText": "Đang lấy hàng"
}
```

**Test 2: Đang giao hàng**
```json
{
  "OrderCode": "GHN123456",
  "Status": "delivering",
  "StatusText": "Đang giao hàng"
}
```

**Test 3: Giao thành công**
```json
{
  "OrderCode": "GHN123456",
  "Status": "delivered",
  "StatusText": "Đã giao hàng"
}
```

---

## 🔒 BẢO MẬT WEBHOOK

### **1. Xác thực Token (Recommended):**

```java
@PostMapping("/ghn")
public ResponseEntity<?> handleGHNWebhook(
        @RequestHeader("X-GHN-Token") String token,
        @RequestBody GHNWebhookRequest request) {
    
    // Verify token
    if (!ghnToken.equals(token)) {
        return ResponseEntity.status(401).body("Unauthorized");
    }
    
    // Process webhook...
}
```

### **2. Verify IP Address:**

```java
// Chỉ chấp nhận webhook từ IP của GHN
private static final List<String> GHN_IPS = Arrays.asList(
    "103.191.144.0/24",
    "103.191.145.0/24"
);
```

---

## 📱 HIỂN THỊ TRÊN FRONTEND

### **Trang chi tiết đơn hàng đã được cập nhật:**

```tsx
{/* Thời gian giao hàng dự kiến */}
{order.ghnExpectedDeliveryTime && (
  <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
    <p className="text-sm text-blue-800 font-medium flex items-center">
      <FiClock className="mr-2" />
      Thời gian giao hàng dự kiến
    </p>
    <p className="font-bold text-blue-900 mt-1">
      {formatDate(order.ghnExpectedDeliveryTime)}
    </p>
  </div>
)}

{/* Mã vận đơn GHN */}
{order.ghnOrderCode && (
  <div className="p-3 bg-gray-50 rounded-lg">
    <p className="text-sm text-gray-600">Mã vận đơn GHN</p>
    <p className="font-mono font-bold text-gray-900">{order.ghnOrderCode}</p>
    {order.ghnShippingStatus && (
      <p className="text-sm text-gray-600 mt-1">
        Trạng thái: <span className="font-medium">{order.ghnShippingStatus}</span>
      </p>
    )}
  </div>
)}
```

### **Component GHNTracking hiển thị chi tiết:**

- ✅ Trạng thái hiện tại
- ✅ Vị trí hiện tại
- ✅ Thời gian giao hàng dự kiến
- ✅ Lịch sử di chuyển
- ✅ Tiền COD
- ✅ Phí vận chuyển

---

## 🎯 KẾT QUẢ

### **Sau khi cấu hình webhook:**

1. ✅ **Tự động cập nhật** khi tài xế đến lấy hàng
2. ✅ **Hiển thị thời gian** giao hàng dự kiến
3. ✅ **Theo dõi real-time** trạng thái vận chuyển
4. ✅ **Lịch sử di chuyển** đầy đủ
5. ✅ **Thông báo** cho khách hàng (có thể thêm)

### **Khách hàng thấy:**

```
📦 Đơn hàng ORD20241220001

🚚 Trạng thái vận chuyển
   ✅ Đang lấy hàng
   📍 Kho Hà Nội
   ⏰ Dự kiến giao: 22/12/2024 10:00

📋 Lịch sử di chuyển:
   • 20/12/2024 10:30 - Đang lấy hàng (Kho Hà Nội)
   • 20/12/2024 09:00 - Chờ lấy hàng
   • 19/12/2024 15:00 - Đơn hàng đã tạo
```

---

## 🔧 TROUBLESHOOTING

### **Webhook không hoạt động:**

1. ✅ Kiểm tra URL webhook đã đăng ký đúng chưa
2. ✅ Kiểm tra server có public IP/domain chưa
3. ✅ Kiểm tra firewall có chặn không
4. ✅ Xem log backend có nhận request không
5. ✅ Test bằng Postman trước

### **Trạng thái không cập nhật:**

1. ✅ Kiểm tra `ghnOrderCode` có đúng không
2. ✅ Xem log có lỗi gì không
3. ✅ Kiểm tra mapping status có đúng không

---

**Webhook GHN giúp hệ thống cập nhật trạng thái tự động, không cần polling!** 🚀
