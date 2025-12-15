# 🔍 Hướng dẫn kiểm tra tích hợp GHN

## ❓ Vấn đề: Đặt hàng xong không thấy có gì thay đổi

Sau khi đặt hàng, bạn không thấy mã vận đơn GHN được tạo. Có thể do một trong các nguyên nhân sau:

## ✅ Checklist kiểm tra

### 1. Kiểm tra điều kiện tạo đơn GHN

Đơn GHN **CHỈ được tạo** khi:
- ✅ `shippingFee > 0` (không phải miễn phí ship)
- ✅ Không phải nội thành Hà Nội (Ba Đình, Hoàn Kiếm, Đống Đa, Hai Bà Trưng, Cầu Giấy, Tây Hồ, Thanh Xuân)

**Nếu đặt hàng nội thành HN → KHÔNG tạo đơn GHN** (giao hàng nội bộ)

### 2. Kiểm tra logs khi đặt hàng

Sau khi đặt hàng, xem logs trong console:

```
✅ Logs thành công:
Creating GHN order for ORD20231212001
✅ GHN order created: GHNABCD1234

❌ Logs lỗi:
❌ Failed to create GHN order for ORD20231212001: [error message]
```

### 3. Kiểm tra database

Sau khi đặt hàng, query database:

```sql
SELECT 
    order_code,
    status,
    shipping_fee,
    ghn_order_code,
    ghn_shipping_status,
    ghn_created_at
FROM orders 
WHERE order_code = 'ORD20231212001';
```

**Kết quả mong đợi:**
- `ghn_order_code`: Có giá trị (VD: "GHNABCD1234")
- `ghn_shipping_status`: "created"
- `ghn_created_at`: Có timestamp

**Nếu NULL** → Đơn GHN không được tạo

### 4. Kiểm tra cấu hình GHN

File: `src/main/resources/application.properties`

```properties
# Kiểm tra các giá trị này
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api
ghn.api.token=76016947-d1a8-11f0-a3d6-dac90fb956b5
ghn.shop.id=198347
ghn.pick.district.id=1485
```

**Test token GHN:**
```bash
curl -X POST https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee \
  -H "Token: 76016947-d1a8-11f0-a3d6-dac90fb956b5" \
  -H "ShopId: 198347" \
  -H "Content-Type: application/json" \
  -d '{
    "service_type_id": 2,
    "from_district_id": 1485,
    "to_district_id": 1542,
    "weight": 1000,
    "insurance_value": 0
  }'
```

Nếu trả về `{"code": 200}` → Token hợp lệ

### 5. Test tạo đơn GHN thủ công

**Bước 1: Đặt hàng với địa chỉ ngoài HN**

```json
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "province": "Bắc Ninh",
  "district": "Từ Sơn",
  "ward": "Đông Ngàn",
  "address": "123 Đường ABC",
  "note": "Test GHN",
  "paymentMethod": "COD"
}
```

**Bước 2: Kiểm tra response**

```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "orderId": 123,
    "orderCode": "ORD20231212001",
    "status": "CONFIRMED",
    "ghnOrderCode": "GHNABCD1234",  // ← Phải có giá trị này
    "ghnShippingStatus": "created",
    "ghnCreatedAt": "2023-12-12T10:30:00",
    "ghnExpectedDeliveryTime": "2023-12-14T18:00:00"
  }
}
```

**Nếu `ghnOrderCode` = null** → Có lỗi

### 6. Kiểm tra API xem trạng thái

```bash
GET /api/orders/{orderId}/shipping-status
Authorization: Bearer <token>
```

**Response mong đợi:**
```json
{
  "success": true,
  "message": "Trạng thái vận chuyển",
  "data": {
    "orderCode": "GHNABCD1234",
    "status": "ready_to_pick",
    "statusText": "Chờ lấy hàng",
    "logs": [...]
  }
}
```

**Nếu lỗi:**
```json
{
  "success": false,
  "message": "Đơn hàng này không có mã vận đơn GHN"
}
```

## 🐛 Các lỗi thường gặp

### Lỗi 1: Token GHN không hợp lệ

**Triệu chứng:**
```
❌ Failed to create GHN order: 401 Unauthorized
```

**Giải pháp:**
1. Kiểm tra token trong `application.properties`
2. Đăng nhập GHN Dashboard: https://dev-online-gateway.ghn.vn/
3. Lấy token mới từ Settings → API Token
4. Cập nhật vào config

### Lỗi 2: Shop ID không đúng

**Triệu chứng:**
```
❌ Failed to create GHN order: Shop not found
```

**Giải pháp:**
1. Vào GHN Dashboard → Settings → Shop Info
2. Copy Shop ID
3. Cập nhật `ghn.shop.id` trong config

### Lỗi 3: District ID không hợp lệ

**Triệu chứng:**
```
❌ Failed to create GHN order: Invalid district
```

**Giải pháp:**
- Hiện tại code dùng default district ID = 1485 (Hà Đông)
- Cần implement logic mapping province/district → district ID
- Tham khảo: `GHNDistrictMapper.java`

### Lỗi 4: Đặt hàng nội thành HN

**Triệu chứng:**
- `ghnOrderCode` = null
- Không có log "Creating GHN order"

**Giải pháp:**
- Đây là **ĐÚNG** behavior
- Nội thành HN = miễn phí ship = không tạo đơn GHN
- Giao hàng nội bộ

**Test với địa chỉ ngoài HN:**
- Bắc Ninh, Hải Phòng, Hải Dương, Vĩnh Phúc...

### Lỗi 5: Webhook không hoạt động

**Triệu chứng:**
- Đơn GHN đã giao nhưng status vẫn là SHIPPING

**Giải pháp:**
1. Kiểm tra webhook URL đã đăng ký trên GHN Dashboard
2. Endpoint phải public: `/api/webhooks/ghn`
3. Nếu local development, dùng ngrok:
   ```bash
   ngrok http 8080
   # Đăng ký: https://abc123.ngrok.io/api/webhooks/ghn
   ```

## 🧪 Test flow hoàn chỉnh

### Test Case 1: Đặt hàng COD ngoài HN

```bash
# 1. Đặt hàng
POST /api/orders
{
  "province": "Bắc Ninh",
  "district": "Từ Sơn",
  "address": "123 ABC",
  "paymentMethod": "COD"
}

# 2. Kiểm tra response có ghnOrderCode

# 3. Xem trạng thái
GET /api/orders/{orderId}/shipping-status

# 4. Kiểm tra database
SELECT * FROM orders WHERE order_code = 'ORD...';
```

**Kết quả mong đợi:**
- ✅ `ghnOrderCode` có giá trị
- ✅ `ghnShippingStatus` = "created"
- ✅ API shipping-status trả về thông tin từ GHN

### Test Case 2: Đặt hàng nội thành HN

```bash
POST /api/orders
{
  "province": "Hà Nội",
  "district": "Ba Đình",
  "address": "456 XYZ",
  "paymentMethod": "COD"
}
```

**Kết quả mong đợi:**
- ✅ Đơn hàng được tạo thành công
- ✅ `ghnOrderCode` = NULL (không tạo GHN)
- ✅ `shippingFee` = 0

## 📊 Debug checklist

Khi gặp vấn đề, kiểm tra theo thứ tự:

1. ☐ Địa chỉ giao hàng có phải nội thành HN không?
2. ☐ `shippingFee` có > 0 không?
3. ☐ Logs có hiển thị "Creating GHN order" không?
4. ☐ Logs có lỗi gì không?
5. ☐ Token GHN có hợp lệ không?
6. ☐ Shop ID có đúng không?
7. ☐ Database có `ghnOrderCode` không?
8. ☐ API `/shipping-status` trả về gì?

## 🔧 Cách fix nhanh

### Nếu muốn test ngay:

**Option 1: Đặt hàng với địa chỉ ngoài HN**
```
Province: Bắc Ninh
District: Từ Sơn
→ Sẽ tạo đơn GHN
```

**Option 2: Tắt check nội thành HN (để test)**

File: `OrderServiceImpl.java` (line ~150)

```java
// Tạm thời comment điều kiện này để test
// if (shippingFee > 0 && !shippingService.isHanoiInnerCity(...)) {
if (shippingFee > 0) {  // Luôn tạo GHN nếu có phí ship
    // Create GHN order
}
```

**Option 3: Force tạo GHN cho mọi đơn (để test)**

```java
// Luôn tạo GHN (bỏ qua mọi điều kiện)
try {
    log.info("Creating GHN order for {}", orderCode);
    // ... code tạo GHN
}
```

## 📞 Cần hỗ trợ thêm?

Nếu vẫn không hoạt động, cung cấp:
1. Logs khi đặt hàng
2. Response từ API `/api/orders`
3. Kết quả query database
4. Địa chỉ giao hàng đã dùng để test

---

**Tóm tắt:** Đơn GHN chỉ được tạo khi giao hàng ngoài nội thành HN và có phí ship > 0. Nếu đặt hàng nội thành HN thì không tạo đơn GHN (đây là behavior đúng).
