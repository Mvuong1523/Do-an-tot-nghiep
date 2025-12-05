# ✅ GHN Integration - Bước 3: Webhook nhận callback từ GHN

## 📋 Tổng quan

Đã hoàn thành webhook endpoint để nhận real-time updates từ GHN khi trạng thái đơn hàng thay đổi. Không cần polling, hệ thống tự động cập nhật khi GHN push notification.

## 🎯 Tính năng đã triển khai

### 1. DTO Class

**GHNWebhookRequest.java**
- Chứa thông tin GHN gửi về khi có thay đổi
- Bao gồm:
  - `orderCode`: Mã vận đơn GHN
  - `status`: Trạng thái mới
  - `statusText`: Mô tả trạng thái
  - `updatedDate`: Thời gian cập nhật (Unix timestamp)
  - `currentWarehouse`: Kho hiện tại
  - `description`: Mô tả chi tiết
  - `reason`: Lý do (nếu có)
  - `codAmount`: Số tiền COD
  - `shippingFee`: Phí vận chuyển
  - `partnerCode`: Mã đơn hàng của shop (orderCode)

### 2. WebhookController

**Endpoint: `POST /api/webhooks/ghn`**
- Public endpoint (không cần authentication)
- Nhận POST request từ GHN
- Log chi tiết request để debug
- Luôn trả về success (tránh GHN retry)
- Xử lý error gracefully

### 3. WebhookService

**Method: `handleGHNWebhook(GHNWebhookRequest request)`**
- Tìm order theo `ghnOrderCode`
- Cập nhật `ghnShippingStatus`
- Tự động cập nhật `status` của Order dựa trên GHN status
- Update timestamps tương ứng
- Transaction-safe

**Logic mapping GHN status → Order status:**

| GHN Status | Order Status | Action |
|-----------|-------------|--------|
| `ready_to_pick`, `picking` | `CONFIRMED` | Chờ/đang lấy hàng |
| `picked`, `storing`, `transporting`, `sorting` | `SHIPPING` | Đã lấy, đang vận chuyển |
| `delivering`, `money_collect_delivering` | `SHIPPING` | Đang giao hàng |
| `delivered` | `DELIVERED` | Giao thành công, mark as PAID |
| `delivery_fail` | `SHIPPING` | Giao thất bại, giữ nguyên |
| `waiting_to_return`, `return`, `returning` | `SHIPPING` | Đang trả hàng |
| `returned` | `CANCELLED` | Đã trả về shop |
| `cancel` | `CANCELLED` | Đơn bị hủy |
| `exception`, `damage`, `lost` | Giữ nguyên | Log error, cần xử lý thủ công |

### 4. Security Configuration

**Whitelist webhook endpoint:**
- `/api/webhooks/**` → permitAll()
- CORS configuration cho phép all origins
- Không cần Bearer token

### 5. OrderRepository

**Method mới: `findByGhnOrderCode(String ghnOrderCode)`**
- Tìm order theo mã vận đơn GHN
- Dùng trong webhook để map GHN order → internal order

## 📝 Logic hoạt động

1. **GHN có thay đổi trạng thái** → Push webhook đến server
2. **Server nhận request** → `POST /api/webhooks/ghn`
3. **Log request** → Ghi lại toàn bộ thông tin
4. **Tìm order** → Query by `ghnOrderCode`
5. **Cập nhật GHN status** → Lưu `ghnShippingStatus`
6. **Map sang Order status:**
   - `delivered` → `DELIVERED` + `PAID`
   - `returned` → `CANCELLED`
   - `cancel` → `CANCELLED`
   - Các status khác → `SHIPPING` hoặc giữ nguyên
7. **Update timestamps:**
   - `confirmedAt` khi chuyển sang CONFIRMED
   - `shippedAt` khi chuyển sang SHIPPING
   - `deliveredAt` khi DELIVERED
   - `cancelledAt` khi CANCELLED
8. **Save order** → Commit transaction
9. **Return success** → Luôn trả về 200 OK

## 🧪 Test

### Test case 1: Webhook delivered
```bash
POST /api/webhooks/ghn
Content-Type: application/json

{
  "orderCode": "GHNABCD1234",
  "status": "delivered",
  "statusText": "Đã giao hàng",
  "updatedDate": 1701849600,
  "currentWarehouse": "Kho Hà Nội",
  "description": "Giao hàng thành công",
  "codAmount": 500000,
  "shippingFee": 30000,
  "partnerCode": "ORD20231205001"
}

Expected:
- Order status → DELIVERED
- Payment status → PAID
- deliveredAt → now
- ghnShippingStatus → "delivered"
```

### Test case 2: Webhook returned
```bash
POST /api/webhooks/ghn

{
  "orderCode": "GHNABCD1234",
  "status": "returned",
  "statusText": "Đã trả hàng",
  "description": "Khách không nhận hàng",
  "reason": "Khách hủy đơn"
}

Expected:
- Order status → CANCELLED
- cancelledAt → now
- cancelReason → "Trả hàng từ GHN"
- ghnShippingStatus → "returned"
```

### Test case 3: Webhook delivering
```bash
POST /api/webhooks/ghn

{
  "orderCode": "GHNABCD1234",
  "status": "delivering",
  "statusText": "Đang giao hàng",
  "currentWarehouse": "Bưu cục Bắc Ninh"
}

Expected:
- Order status → SHIPPING
- shippedAt → now (if not set)
- ghnShippingStatus → "delivering"
```

### Test case 4: Webhook với order không tồn tại
```bash
POST /api/webhooks/ghn

{
  "orderCode": "GHNNOTFOUND",
  "status": "delivered"
}

Expected:
- Log warning: "Order not found"
- Return 200 OK (không fail)
```

## 🔧 Cấu hình GHN Webhook

### 1. Đăng ký webhook URL trên GHN Dashboard

Truy cập: https://dev-online-gateway.ghn.vn/

1. Vào **Settings** → **Webhook**
2. Nhập URL: `https://your-domain.com/api/webhooks/ghn`
3. Chọn events muốn nhận:
   - Order status changed
   - Order delivered
   - Order returned
   - Order cancelled
4. Save

### 2. Test webhook (Development)

Dùng ngrok để expose local server:
```bash
ngrok http 8080
```

Lấy URL: `https://abc123.ngrok.io`

Đăng ký webhook: `https://abc123.ngrok.io/api/webhooks/ghn`

### 3. Webhook signature (Optional - Nâng cao)

Để bảo mật, GHN có thể gửi kèm signature. Cần verify:
```java
String signature = request.getHeader("X-GHN-Signature");
// Verify signature với secret key
```

## 📊 Logs mẫu

```
=== GHN Webhook Received ===
Order Code: GHNABCD1234
Status: delivered (Đã giao hàng)
Partner Code: ORD20231205001
Description: Giao hàng thành công

Processing GHN webhook for order: GHNABCD1234, status: delivered
Found order: ORD20231205001 (SHIPPING)
✅ Updated order ORD20231205001 - Status: DELIVERED, GHN Status: delivered
✅ Order ORD20231205001 delivered successfully
```

## 🔄 Flow hoàn chỉnh

```
1. Khách đặt hàng
   ↓
2. Tạo đơn GHN (Bước 1)
   ↓
3. GHN lấy hàng → Webhook: "picked"
   → Order status: SHIPPING
   ↓
4. GHN vận chuyển → Webhook: "transporting"
   → Order status: SHIPPING
   ↓
5. GHN giao hàng → Webhook: "delivering"
   → Order status: SHIPPING
   ↓
6. Giao thành công → Webhook: "delivered"
   → Order status: DELIVERED
   → Payment status: PAID
```

## ⚠️ Lưu ý

1. **Idempotency**: GHN có thể gửi duplicate webhooks, cần handle
2. **Error handling**: Luôn return 200 OK, tránh GHN retry vô hạn
3. **Logging**: Log đầy đủ để debug khi có vấn đề
4. **Transaction**: Dùng `@Transactional` để đảm bảo data consistency
5. **Security**: Trong production, nên verify webhook signature
6. **Retry logic**: Nếu xử lý fail, có thể queue lại để retry sau

## 🎉 Hoàn thành tích hợp GHN

Đã hoàn thành đầy đủ 3 bước:

✅ **Bước 1**: Tạo đơn GHN khi khách đặt hàng
✅ **Bước 2**: API xem trạng thái vận chuyển
✅ **Bước 3**: Webhook nhận callback từ GHN

Hệ thống giờ đã có:
- Tự động tạo đơn vận chuyển
- Tracking real-time
- Auto-update status
- Full integration với GHN API

## 🔜 Tính năng mở rộng (Optional)

1. **In nhãn vận chuyển**: API `/v2/a5/gen-token` để in tem
2. **Hủy đơn GHN**: API `/v2/switch-status/cancel` khi khách hủy
3. **Webhook signature verification**: Tăng bảo mật
4. **Notification**: Gửi email/SMS cho khách khi có update
5. **Admin dashboard**: Hiển thị tracking info trực quan
