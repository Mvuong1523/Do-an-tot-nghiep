# ✅ GHN Integration - Bước 1: Tạo đơn GHN khi khách đặt hàng

## 📋 Tổng quan

Đã hoàn thành tích hợp tự động tạo đơn vận chuyển GHN khi khách hàng đặt hàng.

## 🎯 Tính năng đã triển khai

### 1. DTO Classes

**CreateGHNOrderRequest.java**
- Chứa thông tin cần thiết để tạo đơn GHN
- Bao gồm: thông tin người nhận, địa chỉ, COD amount, kích thước/trọng lượng, danh sách sản phẩm

**CreateGHNOrderResponse.java**
- Chứa kết quả từ GHN API
- Bao gồm: orderCode, status, expectedDeliveryTime, sortCode, totalFee

### 2. ShippingService

**Method mới: `createGHNOrder(CreateGHNOrderRequest request)`**
- Gọi API GHN: `/v2/shipping-order/create`
- Xử lý response và trả về CreateGHNOrderResponse
- Log chi tiết request/response để debug

### 3. OrderService Integration

**Tự động tạo đơn GHN trong `createOrderFromCart()`**
- Chỉ tạo đơn GHN khi:
  - Phí ship > 0 (không phải miễn phí ship)
  - Không phải nội thành Hà Nội
- Lưu thông tin GHN vào Order entity:
  - `ghnOrderCode`: Mã vận đơn GHN
  - `ghnShippingStatus`: "created"
  - `ghnCreatedAt`: Thời gian tạo
  - `ghnExpectedDeliveryTime`: Thời gian giao dự kiến
- Nếu tạo GHN thất bại → Không fail toàn bộ đơn hàng, chỉ log error

### 4. OrderResponse

**Thêm các field GHN:**
```java
private String ghnOrderCode;
private String ghnShippingStatus;
private LocalDateTime ghnCreatedAt;
private LocalDateTime ghnExpectedDeliveryTime;
```

## 🔧 Cấu hình GHN

Trong `application.properties`:
```properties
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api
ghn.api.token=76016947-d1a8-11f0-a3d6-dac90fb956b5
ghn.shop.id=198347
ghn.pick.district.id=1485
```

## 📝 Logic hoạt động

1. **Khách đặt hàng** → `OrderService.createOrderFromCart()`
2. **Tạo Order** → Lưu vào database
3. **Kiểm tra điều kiện:**
   - Nếu shippingFee > 0 và không phải nội thành HN
   - → Tạo đơn GHN
4. **Build GHN request:**
   - Thông tin người nhận: tên, SĐT, địa chỉ
   - COD amount: Nếu COD → total, nếu online → 0
   - Payment type: COD → 2 (người nhận trả), Online → 1 (shop trả)
   - Items: Danh sách sản phẩm
5. **Gọi GHN API** → Nhận orderCode
6. **Cập nhật Order** → Lưu ghnOrderCode, status, timestamps

## 🧪 Test

### Test case 1: Đặt hàng COD (ngoài nội thành HN)
```
POST /api/orders
{
  "province": "Bắc Ninh",
  "district": "Từ Sơn",
  "address": "123 Đường ABC",
  "shippingFee": 30000,
  "paymentMethod": "COD"
}

Expected:
- Order được tạo
- ghnOrderCode có giá trị
- ghnShippingStatus = "created"
- COD amount = total
```

### Test case 2: Đặt hàng Online (ngoài nội thành HN)
```
POST /api/orders
{
  "province": "Hải Phòng",
  "district": "Hồng Bàng",
  "address": "456 Đường XYZ",
  "shippingFee": 35000,
  "paymentMethod": "SEPAY"
}

Expected:
- Order được tạo
- ghnOrderCode có giá trị
- COD amount = 0
- Payment type = 1 (shop trả phí ship)
```

### Test case 3: Đặt hàng nội thành HN (miễn phí ship)
```
POST /api/orders
{
  "province": "Hà Nội",
  "district": "Ba Đình",
  "address": "789 Đường DEF",
  "shippingFee": 0,
  "paymentMethod": "COD"
}

Expected:
- Order được tạo
- ghnOrderCode = null (không tạo đơn GHN)
- Giao hàng nội bộ
```

## 📊 Response mẫu

```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "orderId": 123,
    "orderCode": "ORD20231205001",
    "status": "CONFIRMED",
    "total": 500000,
    "shippingFee": 30000,
    "ghnOrderCode": "GHNABCD1234",
    "ghnShippingStatus": "created",
    "ghnCreatedAt": "2023-12-05T10:30:00",
    "ghnExpectedDeliveryTime": "2023-12-07T18:00:00"
  }
}
```

## ⚠️ Lưu ý

1. **Error handling**: Nếu GHN API fail, đơn hàng vẫn được tạo thành công
2. **District ID**: Hiện tại dùng default 1485 (Hà Đông), cần improve logic mapping
3. **Ward code**: Chưa implement, để trống
4. **Weight/Dimensions**: Dùng giá trị mặc định (1kg, 20x20x10cm)

## 🔜 Bước tiếp theo

**Bước 2: API xem trạng thái vận chuyển**
- Endpoint: `GET /api/orders/{orderCode}/shipping-status`
- Gọi GHN API: `/v2/shipping-order/detail`
- Hiển thị trạng thái real-time từ GHN

**Bước 3: Webhook nhận callback từ GHN**
- Endpoint: `POST /api/webhooks/ghn`
- Tự động cập nhật trạng thái khi GHN callback
- Update `ghnShippingStatus` và `status` của Order
